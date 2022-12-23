#!/usr/bin/env bb

;;based on: https://github.com/sinsongdev/gradle-cash-to-maven-repo/blob/master/gradle_cache_to_repo.py

(require '[clojure.tools.cli :refer [parse-opts]])

(def cli-options
  [["-s" "--src DIRECTORY" "Gradle cache directory"
    :missing "Must provide a source directory"]
   ["-d" "--dest DIRECTORY" "Maven repository directory"
    :missing "Must provide a destination directory"]
   ["-a" "--artifact ARTIFACT" "Artifact id"]])

(def parsed-args (parse-opts *command-line-args* cli-options))
(def options (:options parsed-args))

(def src (:src options))
(def dest (:dest options))

(def filter
  (if-let [filter-option (:artifact options)]
    (str/split filter-option #":")))

(def group-filter (nth filter 0 nil))
(def artifact-filter (nth filter 1 nil))
(def version-filter (nth filter 2 nil))

(defn- create-path-string [& segments]
  (str/join fs/file-separator segments))

(defn- get-dest-dir-string [group artifact version]
  (create-path-string
   dest
   (str/replace group #"\." fs/file-separator)
   artifact
   version))

(defn- process-version [group artifact version]
  (if (or (nil? version-filter) (= version version-filter))
    (let [version-dir-string (create-path-string src group artifact version)
          version-dir (fs/file version-dir-string)
          hash-dirs (fs/list-dir version-dir)
          dest-dir-string (get-dest-dir-string group artifact version)]
      (fs/create-dirs (fs/file dest-dir-string))
      (doseq [hash-dir hash-dirs
            file (fs/list-dir hash-dir)]
        (fs/copy file (fs/file (create-path-string dest-dir-string (fs/file-name file))) {:replace-existing true})))))
 
(defn- process-artifact [group artifact]
  (if (or (nil? artifact-filter) (= artifact artifact-filter))
    (let [artifact-dir-string (create-path-string src group artifact)
          artifact-dir (fs/file artifact-dir-string)]
      (doseq [version-dir (fs/list-dir artifact-dir)]
        (process-version group artifact (fs/file-name version-dir))))))

(defn- process-group [group]
  (if (or (nil? group-filter) (= group group-filter))
    (let [group-dir-string (create-path-string src group)
          group-dir (fs/file group-dir-string)]
      (doseq [artifact-dir (fs/list-dir group-dir)]
        (process-artifact group (fs/file-name artifact-dir))))))

(defn- process-cache []
  (doseq [group-dir (fs/list-dir (fs/file src))]
    (process-group (fs/file-name group-dir))))

(if-let [errors (:errors parsed-args)]
  (doseq [error errors] (println (str "Error: " error)))
  (process-cache))
