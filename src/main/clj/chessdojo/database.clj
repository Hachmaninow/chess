(ns chessdojo.database
  (:require [clojure.java.io :as io]))

(def base-path "games/deflated/")

(defn load-dgn [name]
  (read-string (slurp (io/resource (str base-path name ".dgn")))))
