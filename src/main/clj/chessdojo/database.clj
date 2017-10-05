(ns chessdojo.database
  (:require [clojure.java.io :as io]
            [clojure.set :refer [rename-keys]]
            [chessdojo.data :as cd]
            [monger.core :as mg]
            [monger.collection :as mc]
            [environ.core :refer [env]]
            [chessdojo.game :as cg])
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
    :game-info (cg/game-info game)
    :_id (create-id)))


(defn store-game-record [game-record]
  (mc/insert-and-return @db collection game-record))

(defn list-games []
  (mc/find-maps @db collection {}))

(defn restore-game-record [id]
  (mc/find-one-as-map @db collection {:_id id}))

;(list-games)

;
; db.games.find({}, {_id:1})
; db.games.remove({})
;
