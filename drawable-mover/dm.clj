#!/usr/bin/env bb

(require '[clojure.tools.cli :refer [parse-opts]])

(def cli-options
  [["-s" "--source DIRECTORY" "Source directory containing the drawable folders"]
   ["-d" "--destination DIRECTORY" "Destination directory where the drawables should be moved to"]
   ["-n" "--name DRAWABLE_NAME" "The name of the drawable to move WITHOUT file extension"]])

(def options (:options (parse-opts *command-line-args* cli-options)))
(def drawable-folder-regex #"drawable-?(.*)?")
(def drawable-file-extension-regex-str "\\.[a-zA-Z]+")

(let [source-directory (io/file (:source options))
      destination-directory (io/file (:destination options))
      drawable-name (:name options)
      source-drawable-directories (filter #(re-matches drawable-folder-regex (.getName %)) (.listFiles source-directory))]
  (doseq [folder source-drawable-directories]
    (let [files-to-move (filter #(re-matches (re-pattern (str drawable-name drawable-file-extension-regex-str)) (.getName %)) (.listFiles folder))]
      (when-not (empty? files-to-move)
        (let [file-to-move (first files-to-move)
              target-file (io/file (.getAbsolutePath destination-directory) (.getName folder) (.getName file-to-move))]
          (println (str "moving " (.getName (.getParentFile file-to-move)) fs/file-separator (.getName file-to-move)))
          (.mkdirs (.getParentFile target-file))
          (.renameTo file-to-move target-file))))))
