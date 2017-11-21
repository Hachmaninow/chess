(ns chessdojo.taxonomy
  (:require [environ.core :refer [env]]
            [chessdojo.database :as cdb]))

(defn- insert-children [taxon by-parent]
  (let [children (map #(insert-children % by-parent) (get by-parent (:_id taxon)))]
    (if (not-empty children)
      (assoc taxon :children children) taxon)))

(defn organize-taxonomy []
  (let [taxa (cdb/load-taxa)
        by-parent (group-by :parent taxa)]
    (map #(insert-children % by-parent) (filter #(nil? (:parent %)) taxa))))

