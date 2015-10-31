(ns chess.data
  (:require [me.raynes.fs :as fs]))

(def base-path "/Users/hman/Projects/labs/clojure/chess/src/data")

(defn file-type [file]
  (cond
    (fs/directory? file) :directory
    (= 'manifest' (fs/name file)) :manifest
    :else :game))

(defn index-contents [path]
  (group-by file-type (fs/list-dir (str base-path path)))
  )

(index-contents "/01.opening")