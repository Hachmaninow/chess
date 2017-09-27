(ns chessdojo.database
  (:require [clojure.java.io :as io]
            [clojure.set :refer [rename-keys]]
            [chessdojo.data :as cd]
            [monger.core :as mg]
            [monger.collection :as mc]
            [environ.core :refer [env]])
  (:import (java.util UUID)))

; to start mongo-db locally: docker run --name mongo -d -p 27017:27017 mongo

(def database (env :mongo-database-name))

(def collection (env :mongo-collection-name))

(def db
  (let [conn (mg/connect) db (mg/get-db conn database)]
    (atom db)))

(defn create-id [] (str (UUID/randomUUID)))

(defn init-game-record [game]
  (hash-map
    :dgn (pr-str (cd/deflate game))
    :id (create-id)))

(defn- rename-internal-id [game-data]
  (rename-keys game-data {:_id :id}))

(defn- rename-external-id [game-data]
  (rename-keys game-data {:id :_id}))

(defn store-game-record [game-data]
  (rename-internal-id (mc/insert-and-return @db collection (rename-external-id game-data))))

(defn list-games []
  (map rename-internal-id (mc/find-maps @db collection {})))

(defn restore-game-record [id]
  (rename-internal-id (mc/find-one-as-map @db collection {:_id id})))

;(list-games)

;
; db.games.find({}, {_id:1})
; db.games.remove({})
;
