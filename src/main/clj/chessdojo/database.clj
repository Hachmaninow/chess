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

(def taxonomy-collection "taxonomy")

(def games-collection "games")


(def db
  (let [conn (mg/connect) db (mg/get-db conn database)]
    (atom db)))

(defn create-id [] (str (UUID/randomUUID)))


; taxonomy tree persistence

(defn init-taxon-record [name parent-id]
  {:_id (create-id)
   :name name
   :parent parent-id})

(defn store-taxon [taxon-record]
  (mc/save-and-return @db taxonomy-collection taxon-record))

(defn load-taxa []
  (mc/find-maps @db taxonomy-collection {}))


; game persistence

(defn init-game-record [game]
  (hash-map
    :dgn (pr-str (cd/deflate game))
    :game-info (cg/game-info game)
    :_id (create-id)))


(defn store-game-record [game-record]
  (mc/save-and-return @db games-collection game-record))

(defn list-games []
  (mc/find-maps @db games-collection {}))

(defn restore-game-record [id]
  (mc/find-one-as-map @db games-collection {:_id id}))

;(list-games)

;
; db.games.find({}, {_id:1})
; db.games.remove({})
;


