(ns chessdojo.data
  (:require [chessdojo.game :as cg]
            [clojure.zip :as zip]))

(defn- deflate-variation [variation-vec]
  (map #(cond
         (vector? %) (deflate-variation %)
         (:move %) (vector (get-in % [:move :from]) (get-in % [:move :to]))
         ) variation-vec))

(defn deflate
  "Transform the given game into a minimal representation that allows later reconstruction."
  [game]
  (deflate-variation (rest (zip/root game))))               ; skip the first element as it's the anchor containing the start position

(defn inflate [deflated]
  (map #(cond
         (and (vector? %) (integer? (first %))) {:from (first %) :to (second %)}
         :default (inflate %)
         ) deflated))

(defn- event-stream [inflated]
  (flatten
    (map #(cond
           (map? %) %
           (seq? %) (vector :back (event-stream %) :out :forward)
           ) inflated)))

(defn load-game [deflated]
  (-> deflated inflate event-stream cg/soak))