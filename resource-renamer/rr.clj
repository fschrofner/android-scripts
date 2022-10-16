#!/usr/bin/env bb

(require '[clojure.tools.cli :refer [parse-opts]])

(def cli-options
  [["-d" "--directory DIRECTORY" "Directory"]
   ["-n" "--name NAME" "Target resource name"]])

(def options (:options (parse-opts *command-line-args* cli-options)))

(defn- rename-child
  [folder target-name]
  (let [files (.listFiles folder)]
    (if (= (count files) 1)
      (let [resource (first files)
            extension (last (str/split (.getName resource) #"\."))]
        (when (nil? extension) (throw (.Exception "file does not have a file extension")))
        (.renameTo resource (io/file (str (.getParent resource) (java.io.File/separator) target-name "." extension))))
      (throw (.Exception "directories need to contain exactly one file")))))

(let [directory (io/file (:directory options))]
  (doseq [subdirectory (.listFiles directory)]
    (rename-child subdirectory (:name options))))
