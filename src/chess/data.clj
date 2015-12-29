(ns chess.data
  (:require [me.raynes.fs :as fs]))

(def base-path "/Users/hman/Projects/labs/clojure/chess/src/data/")

(defn load-pgn [name]
  (slurp (str base-path name ".pgn"))
)