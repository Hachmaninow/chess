(ns chessdojo.database-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [chessdojo.database :refer :all]
            [monger.collection :as mc]
            [chessdojo.data :as cd]
            [clojure.java.io :as io]))

(defn reset-database [f]
  (assert (str/includes? database "test"))
  (mc/remove @db collection {})
  (f))

(use-fixtures :once reset-database)

(def sample-game
  (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp read-string cd/load-game))

(deftest test-game-data
  (let [game-data (new-game-data sample-game)]
    (is (string? (:dgn game-data)))
    (is (= 36 (count (:id game-data))))))

(deftest test-store-game
  (let [stored-game (store-game-data (new-game-data sample-game))]
    (is (map? stored-game))
    (is (string? (:id stored-game)))))

(deftest test-store-restore-game-data
  (let [id (:id (store-game-data (new-game-data sample-game)))]
    (is (= sample-game (cd/load-game (read-string (:dgn (restore-game-data id))))))))

(deftest test-game-list
  (let [id (:id (store-game-data (new-game-data sample-game)))
        list-of-oids (set (map :id (list-games)))]
    (is (true? (contains? list-of-oids id)))))


