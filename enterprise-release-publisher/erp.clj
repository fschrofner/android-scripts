#! /usr/bin/bb
(require '[babashka.curl :as curl])

;;executes shell command but throws exception on error
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
      (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

;;gets the working directory, but removes the new line in the end
(def wd (as-> (:out (safe-sh "pwd")) $
          (subs $ 0 (- (count $) 1))))
(def metadata-file "output-metadata.json")
(def config-file "enterprise-releases.json")

(defn- create-path-string [& segments]
  "creates a os independent path string"
  (str/join fs/file-separator segments))

(defn- get-output-folder [module variant]
  "creates the path string of the output folder for the given module & variant"
  (create-path-string wd module "build" "outputs" "apk" variant))

(defn- create-enterprise-release [module variant]
  "builds the app using the given module & variant and then returns the parsed metadata"
  (println "building project..")
  (safe-sh (create-path-string wd "gradlew") (str ":" module ":assemble" (str/capitalize variant)))
  (json/parse-string (slurp (create-path-string (get-output-folder module variant) metadata-file)) true))

(defn- fetch-remote-config [server path file-name]
  "fetches and parses the configuration on the remote server"
  (let [remote-path (str server path "/" file-name ".json")]
    (println "fetching remote config..")
    (json/parse-string (:body (curl/get remote-path)) true)))

(defn- upload-version [config metadata]
  "uploads the built apk and updates the remote json to reflect the new version"
  (let [info (second config)
        server (:server info)
        path (:path info)
        file-name (:fileName info)
        output-folder (get-output-folder (:module info) (:variant info))
        output-file (create-path-string output-folder (get-in metadata [:elements 0 :outputFile]))
        remote-config (fetch-remote-config server path file-name)
        new-version (get-in metadata [:elements 0 :versionCode])
        version-name (get-in metadata [:elements 0 :versionName])]
    (if (<= new-version (get-in remote-config [:android :version_code]))
      (println "warning: version on remote was higher or the same as uploaded version"))
    (println "uploading apk..")
    (curl/post (str server path "/" file-name ".apk") {:raw-args ["-T" output-file]})
    (println "updating version on remote..")
    (let [updated-remote-file (create-path-string output-folder (str file-name ".json"))]
      (as-> remote-config $
        (update-in $ [:android :version_code] (fn [x] new-version))
        (json/generate-string $ {:pretty true})
        (spit updated-remote-file $))
      (curl/post (str server path "/" file-name ".json") {:raw-args ["-T" updated-remote-file]}))
    (println (str "new version " version-name " (" new-version ")" " successfully uploaded to " (name (first config))))))

(defn- add-git-tag [config metadata]
  "creates a new git tag based on the defined format and pushes it to the remote repository"
  (let [unstaged-changes (filter #(not (str/blank? %)) (str/split-lines (:out (safe-sh "git" "status" "-s" "uno"))))
        nr-of-changes (count unstaged-changes)]
    (if (= nr-of-changes 0)
      (let [tag-format (:format config)
            tag-args (map #(get-in metadata [:elements 0 %]) (map #(keyword %) (:arguments config)))
            tag (apply format (conj tag-args tag-format))
            remote "origin"]
        (safe-sh "git" "tag" tag)
        (safe-sh "git" "push" remote tag)
        (println (str "created tag " tag " and pushed it to remote " remote)))
      (println "warning: there were uncommitted changes. git tag was not automatically created"))))

(defn- build-and-upload [config]
  "builds & uploads the application based on the handed over configuration"
  (let [info (second config)
        module (:module info)
        variant (:variant info)
        git-tag (:gitTag info)]
    (let [metadata (create-enterprise-release module variant)]
      (upload-version config metadata)
      (if (not (nil? git-tag))
        (add-git-tag git-tag metadata)))))

(def config (json/parse-string (slurp (create-path-string wd config-file)) true))

;;if there's only one config, pick that one
;;otherwise get the one specified as argument
(def selected-config
  (if
      (= (count config) 1) (first config)
      (first (filter #(= (first %) (keyword (first *command-line-args*))) config))))

(if (nil? selected-config)
  (println "error: no matching config found")
  (build-and-upload selected-config))
