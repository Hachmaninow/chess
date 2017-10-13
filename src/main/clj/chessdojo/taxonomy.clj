(ns chessdojo.taxonomy
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [yaml.core :as yaml]))

(def root-path (io/file "/home/hman/projects/personal/chess-dojo/data"))

(declare process-directory)

(defn- read-directory-meta
  [dir parent-path]
  (let [meta (into {} (yaml/from-file (io/file dir "meta.yml") true))
        path (str parent-path (fs/base-name dir) "/")]
    (assoc meta
      :text (:name meta)
      :id path
      :path path
      :nodes (process-directory dir path))))

(defn- process-directory [full-path parent-path]
  (map #(read-directory-meta % parent-path) (filter fs/directory? (fs/list-dir full-path))))

(defn read-taxonomy
  ([] (read-taxonomy root-path))
  ([path] (process-directory path "")))




