(ns chessdojo.database)

(def base-path "/Users/hman/Projects/labs/clojure/chess/src/clj/data/")

(defn load-pgn [name]
  (slurp (str base-path name ".pgn"))
  )