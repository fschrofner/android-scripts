#!/usr/bin/env bb

(require '[clojure.tools.cli :refer [parse-opts]])

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 5555
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]])

(def options (:options (parse-opts *command-line-args* cli-options)))
(def port (:port options))

(def ip (->> (shell/sh "adb" "shell" "ip addr")
      :out
      (re-find #"inet (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})/.* scope global")
      last))

(shell/sh "adb" "tcpip" (str port))
(def result (shell/sh "adb" "connect" (str ip ":" port)))
(println (:out result))
