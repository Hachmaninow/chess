(ns chessdojo.database-test
  (:require [clojure.test :refer :all]
            [chessdojo.database :refer :all]))

(deftest test-load-pgn
  (is (= 1346 (count (load-pgn "queens-gambit-declined"))))
  )