#!/usr/bin/env bb

(require
 '[clojure.tools.cli :refer [parse-opts]]
 '[clojure.string :as string])

(def cli-options
  [["-h" "--help" "Shows help"]])

(def parsed-params (parse-opts *command-line-args* cli-options))
(def options (:options parsed-params))
(def relative-paths (:arguments parsed-params))

(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (string/trim (:err $)))))))

(defn- error-and-exit [message]
  (println (str "error: " message))
  (System/exit 1))

(defn- usage []
  (println "usage: wfc.clj [RELATIVE_PATH ...]")
  (println "example: wfc.clj local.properties"))

(defn- to-absolute-path [base path]
  (let [file (fs/file path)]
    (if (fs/absolute? file)
      (str (fs/normalize file))
      (str (fs/normalize (fs/file base path))))))

(defn- inside-root? [root path]
  (let [root-path (str (fs/normalize (fs/file root)))
        file-path (str (fs/normalize (fs/file path)))
        root-prefix (str root-path fs/file-separator)]
    (or (= root-path file-path)
        (string/starts-with? file-path root-prefix))))

(defn- resolve-relative-path [root relative-path]
  (let [absolute-path (to-absolute-path root relative-path)]
    (when-not (inside-root? root absolute-path)
      (throw (Exception. (str "path escapes repository root: " relative-path))))
    absolute-path))

(when (:help options)
  (usage)
  (System/exit 0))

(when (seq (:errors parsed-params))
  (doseq [error (:errors parsed-params)]
    (println (str "error: " error)))
  (System/exit 1))

(when (empty? relative-paths)
  (usage)
  (error-and-exit "you need to provide at least one relative file path"))

(try
  (let [is-work-tree (string/trim (:out (safe-sh "git" "rev-parse" "--is-inside-work-tree")))]
    (when-not (= is-work-tree "true")
      (error-and-exit "current directory is not inside a git repository"))

    (let [worktree-root (string/trim (:out (safe-sh "git" "rev-parse" "--show-toplevel")))
          git-dir (string/trim (:out (safe-sh "git" "rev-parse" "--absolute-git-dir")))
          git-common-dir (to-absolute-path worktree-root (string/trim (:out (safe-sh "git" "rev-parse" "--git-common-dir"))))]
      (when (= (str (fs/normalize (fs/file git-dir)))
               (str (fs/normalize (fs/file git-common-dir))))
        (error-and-exit "current directory is not a linked git worktree"))

      (let [source-root (some-> git-common-dir fs/file fs/parent str)
            failed-copies (atom 0)]
        (when (nil? source-root)
          (error-and-exit "could not resolve source repository root"))

        (doseq [relative-path relative-paths]
          (if (fs/absolute? (fs/file relative-path))
            (do
              (println (str "error: expected a relative path, got: " relative-path))
              (swap! failed-copies inc))
            (let [source-file (resolve-relative-path source-root relative-path)
                  destination-file (resolve-relative-path worktree-root relative-path)
                  source-file-handle (fs/file source-file)]
              (cond
                (not (fs/exists? source-file-handle))
                (do
                  (println (str "error: file does not exist in source repository: " relative-path))
                  (swap! failed-copies inc))

                (fs/directory? source-file-handle)
                (do
                  (println (str "error: path is a directory in source repository: " relative-path))
                  (swap! failed-copies inc))

                :else
                (do
                  (when-let [destination-parent (some-> destination-file fs/file fs/parent)]
                    (fs/create-dirs destination-parent))
                  (fs/copy source-file destination-file {:replace-existing true})
                  (println (str "copied " relative-path)))))))

        (if (zero? @failed-copies)
          (println (str "successfully copied " (count relative-paths) " file(s)"))
          (error-and-exit (str "failed to copy " @failed-copies " file(s)"))))))
  (catch Exception exception
    (error-and-exit (.getMessage exception))))
