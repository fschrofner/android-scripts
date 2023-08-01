#!/usr/bin/env bb

(require
 '[clojure.tools.cli :refer [parse-opts]]
 '[clojure.string :as string])

(def cli-options
  [["-b" "--bundle BUNDLE" "The bundle id"]])

;;executes shell command but throws exception on error
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

;;gets the working directory, but removes the new line in the end
(def wd (as-> (:out (safe-sh "pwd")) $
          (subs $ 0 (- (count $) 1))))

(def options (:options (parse-opts *command-line-args* cli-options)))

(if-let [bundle (:bundle options)]
  (do
    (def packages (->>
                   (safe-sh "adb" "shell" "pm" "path" bundle)
                   :out
                   (string/split-lines)
                   (map #(last (re-find #"package:(.*)$" %)))))

    (println (str "found " (count packages) " packages, pulling.."))

    (doseq [package packages]
      (safe-sh "adb" "pull" package wd))

    (println "successfully pulled packages.")
    (println "install them to the currently connected device? (y/n): ")

    (def file-names (map #(str wd fs/file-separator (.getName (io/file %))) packages))

    (let [answer (read-line)]
      (when (= answer "y")
        (if (< 1 (count file-names))
          (apply safe-sh "adb" "install-multiple" file-names)
          (safe-sh "adb" "install" (first file-names))))))

  (println "error: you need to provide a bundle id with -b [BUNDLE]"))


