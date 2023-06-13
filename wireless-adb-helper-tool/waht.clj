#!/usr/bin/env bb

(require '[clojure.tools.cli :refer [parse-opts]])

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 5555
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]])

(def options (:options (parse-opts *command-line-args* cli-options)))
(def port (:port options))

;; using wlan0 for now, as this fixes issues with devices having multiple ip addresses (e.g. when using a vpn)
;; it seems like this interface name is used on all devices that i'm using at least
(def ip (->> (shell/sh "adb" "shell" "ip addr")
      :out
      (re-find #"inet (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})/.* scope global wlan0")
      last))

(println (str "determined ip address of device: " ip))

(shell/sh "adb" "tcpip" (str port))
(println (str "opened adb in tcp mode on port: " port))

(def result (shell/sh "adb" "connect" (str ip ":" port)))
(println (:out result))
