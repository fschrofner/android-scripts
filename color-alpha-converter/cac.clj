#!/usr/bin/env bb

(require '[clojure.tools.cli :refer [parse-opts]])
(use '[clojure.string])
(require '[clojure.math :refer [round]])

(def cli-options
  [["-i" "--inverse" "Inverses the conversion so the value is converted to a percentage from a given hex number."
    :id :inverse
    :default false]])

(def step-size 2.55M)
(def parsed-params (parse-opts *command-line-args* cli-options))
(def options (:options parsed-params))
(def value (first (:arguments parsed-params)))

(defn pad-with-zero
  [length string]
  (as-> string $
   (count $)
   (- length $)
   (take $ (repeat "0"))
   (concat $ string)
   (join $)))

(defn to-hex
  [value]
  (->> value
      (Integer/parseInt)
      (* step-size)
      (round)
      (int)
      (format "%x")
      (pad-with-zero 2)))

(defn from-hex
  [value]
  (as-> value $
      (Integer/parseInt $ 16)
      (with-precision 2(/ $ step-size))
      (round $)
      (int $)
      (str $)))

(if (:inverse options)
  (print (from-hex value))
  (print (to-hex value)))
