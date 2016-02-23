(ns chessdojo.database
  (:require [clojure.java.io :as io]
            [chessdojo.data :as cd]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.result :as mr]
            [environ.core :refer [env]])
  (:import [org.bson.types ObjectId])

  )

(def base-path "games/deflated/")


(defn load-dgn [name]
  (read-string (slurp (io/resource (str base-path name ".dgn")))))

(def database (env :mongo-database-name))

(def collection (env :mongo-collection-name))

(def db
  (let [conn (mg/connect) db (mg/get-db conn database)]
    (atom db)))

(def complex-game
  (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp read-string cd/load-game)
  )

(defn store-game [game]
  (let [oid (ObjectId.)
        doc {:_id oid :dgn (cd/deflate game)}
        write-result (mc/insert @db collection doc)]
    (when (mr/acknowledged? write-result) (str oid))))

(defn restore-game [id]
  (let [doc (mc/find-one-as-map @db collection {:_id (ObjectId. id)})]
    (cd/load-game (read-string (:dgn doc)))))

(defn list-games []
  (mc/find-maps @db collection {} ["_id"]))

;
; db.games.find({}, {_id:1})
; db.games.remove({})
;





