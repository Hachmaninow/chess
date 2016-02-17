(ns chessdojo.data
  (:require [chessdojo.game :as cg]
            [chessdojo.notation :as cn]
            [clojure.zip :as zip]
            [chessdojo.rules :as cr]))

(defn- deflate-node [[k v]]
  (case k
    :move (symbol (cn/san v))
    :comment v
    nil))

(defn- deflate-variation [variation-vec]
  (mapcat #(cond
            (vector? %) (list (deflate-variation %))
            (map? %) (remove nil? (map deflate-node %))
            ) variation-vec))

(defn deflate
  "Transform the given game into a minimal string representation that allows efficient reconstruction."
  [game]
  (pr-str (deflate-variation (rest (zip/root game)))))      ; skip the first element as it's the anchor containing the start position

(defn- event-stream [deflated]
  (flatten
    (map #(cond
           (symbol? %) %
           (string? %) {:comment %}
           (sequential? %) (vector :back (event-stream %) :out :forward)
           ) deflated)))

(defn load-game [deflated-str]
  (apply cg/soak (event-stream (read-string deflated-str))))