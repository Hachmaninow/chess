(ns chess.data-test
  (:require [clojure.test :refer :all]
            [chess.data :refer :all]))

(deftest test-load-pgn
  (is (= 1346 (count (load-pgn "queens-gambit-declined"))))
  )