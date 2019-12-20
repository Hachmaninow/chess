(ns chessdojo.squares-test
  (:require [clojure.test :refer :all]
            [chessdojo.squares :refer :all]))

(deftest test-rank
  (is (= 0 (rank a1)))
  (is (= 2 (rank b3)))
  (is (= 5 (rank d6)))
  (is (= 1 (rank e2)))
  (is (= 6 (rank g7)))
  (is (= 7 (rank h8))))

(deftest test-file
  (is (= 0 (file a1)))
  (is (= 1 (file b3)))
  (is (= 3 (file d6)))
  (is (= 4 (file e2)))
  (is (= 6 (file g7)))
  (is (= 7 (file h8))))

(deftest test-distance
  (is (= 0 (distance a1 a1)))
  (is (= 1 (distance a1 b1)))
  (is (= 7 (distance a1 a8)))
  (is (= 7 (distance a1 h8)))
  (is (= 7 (distance a1 h7)))
  (is (= 7 (distance a1 g8)))
  (is (= 6 (distance e2 g8)))
  (is (= 5 (distance e2 a7)))
  (is (= 0 (distance e2 e2))))

(deftest test-all-squares
  (is (= (range 0 64) all-squares)))



(deftest test-sqr-to-str
  (is (= "a1" (sqr-to-str a1)))
  (is (= "a8" (sqr-to-str a8)))
  (is (= "b6" (sqr-to-str b6)))
  (is (= "d5" (sqr-to-str d5)))
  (is (= "e2" (sqr-to-str e2)))
  (is (= "f8" (sqr-to-str f8)))
  (is (= "h1" (sqr-to-str h1)))
  (is (= "h8" (sqr-to-str h8))))