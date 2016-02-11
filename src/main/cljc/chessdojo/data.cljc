(ns chessdojo.data
  (:require [chessdojo.game :as cg]
            [clojure.zip :as zip]))

(defn- deflate-variation [variation-vec]
  (map #(cond
         (vector? %) (deflate-variation %)
         (:move %) (remove nil? (vector
                                  (get-in % [:move :from]) (get-in % [:move :to])
                                  (when (:comment %) (:comment %))
                                  ))
         ) variation-vec))

(defn deflate
  "Transform the given game into a minimal representation that allows efficient reconstruction."
  [game]
  (deflate-variation (rest (zip/root game))))               ; skip the first element as it's the anchor containing the start position

(defn inflate-move-node [[from to & rest]]
  (into {:from from :to to}
        (map
          #(cond
            (string? %) [:comment %]
            ) rest)))

(defn inflate [deflated]
  (map #(cond
         (and (sequential? %) (integer? (first %))) (inflate-move-node %)
         :default (inflate %)
         ) deflated))

(defn- event-stream [inflated]
  (flatten
    (map #(cond
           (map? %) (remove empty? (vector (select-keys % [:from :to]) (select-keys % [:comment])))
           (sequential? %) (vector :back (event-stream %) :out :forward)
           ) inflated)))

(defn load-game [deflated]
  (-> deflated inflate event-stream cg/soak))