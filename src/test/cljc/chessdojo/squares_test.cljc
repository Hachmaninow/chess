(ns chessdojo.squares-test
  (:require #?(:clj [clojure.test :refer :all] :cljs [cljs.test :refer-macros [deftest is testing run-tests]])
               [chessdojo.squares :refer [rank file distance all-squares sqr-to-str a1 b1 c1 d1 e1 f1 g1 h1 a2 b2 c2 d2 e2 f2 g2 h2 a3 b3 c3 d3 e3 f3 g3 h3 a4 b4 c4 d4 e4 f4 g4 h4 a5 b5 c5 d5 e5 f5 g5 h5 a6 b6 c6 d6 e6 f6 g6 h6 a7 b7 c7 d7 e7 f7 g7 h7 a8 b8 c8 d8 e8 f8 g8 h8]]))

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