(ns chessdojo.data
  (:require [chessdojo.game :as cg]
            [chessdojo.notation :as cn]
            [clojure.zip :as zip]
            [chessdojo.rules :as cr]))

(defn- deflate-node [[k v]]
  (case k
    :move (symbol (cn/san v))
    :comment v
    :annotations (map (comp symbol name) (vals v))
    nil))

(defn- deflate-variation [variation-vec]
  (mapcat #(cond
            (vector? %) (list (deflate-variation %))
            (map? %) (remove nil? (flatten (map deflate-node %)))
            ) variation-vec))

(defn deflate
  "Transform the given game into a minimal string representation that allows efficient reconstruction."
  [game]
  (deflate-variation (rest (zip/root game))))      ; skip the first element as it's the anchor containing the start position

(defn- event-stream [deflated]
  (flatten
    (map #(cond
           (sequential? %) (vector :back (event-stream %) :out :forward)
           :default %
           ) deflated)))

(defn load-game [deflated]
  (apply cg/soak (event-stream deflated)))