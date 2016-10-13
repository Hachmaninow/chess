(ns chessdojo.database-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [chessdojo.database :refer :all]
            [monger.collection :as mc]
            [chessdojo.data :as cd]
            [clojure.java.io :as io]))

(defn reset-database []
  (assert (str/includes? database "test"))
  (mc/remove @db collection {}))

(def sample-game
  (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp read-string cd/load-game))

(deftest ^:functional test-game-data
  (reset-database)
  (let [game-data (new-game-data sample-game)]
    (is (string? (:dgn game-data)))
    (is (= 36 (count (:id game-data))))))

(deftest ^:functional test-store-game
  (reset-database)
  (let [stored-game (store-game-data (new-game-data sample-game))]
    (is (map? stored-game))
    (is (string? (:id stored-game)))))

(deftest ^:functional test-store-restore-game-data
  (reset-database)
  (let [id (:id (store-game-data (new-game-data sample-game)))]
    (is (= sample-game (cd/load-game (read-string (:dgn (restore-game-data id))))))))

(deftest ^:functional test-game-list
  (reset-database)
  (let [id (:id (store-game-data (new-game-data sample-game)))
        list-of-oids (set (map :id (list-games)))]
    (is (true? (contains? list-of-oids id)))))


