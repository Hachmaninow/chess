(ns chessdojo.database-test
  (:require [clojure.test :refer :all]
            [chessdojo.database :refer :all]
            [monger.collection :as mc]))

(defn reset-database [f]
  ;(assert (clojure.string/includes? database "test"))
  (mc/remove @db collection {})
  (f)
  )

(use-fixtures :once reset-database)

(deftest test-save-load
  (let [game complex-game]
    (is (= game (restore-game (store-game complex-game))))))

(deftest test-game-list
  (let [oid (store-game complex-game)
        list-of-oids (set (map str (map :_id (list-games))))]
    (is (true? (contains? list-of-oids oid)))))

