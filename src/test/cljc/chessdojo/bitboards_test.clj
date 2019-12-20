(ns chessdojo.bitboards-test
  (:require [clojure.test :refer :all]
            [chessdojo.squares :refer :all]
            [chessdojo.bitboards :refer :all]))

(deftest test-knight-attacks
  (is (= [c2 b3] (scan (knight-attacks a1))))
  (is (= [d1 d3 a4 c4] (scan (knight-attacks b2))))
  (is (= [b1 d1 a2 e2 a4 e4 b5 d5] (scan (knight-attacks c3))))
  (is (= [c5 e5 b6 f6 b8 f8] (scan (knight-attacks d7))))
  (is (= [g3 f4 f6 g7] (scan (knight-attacks h5)))))

(deftest test-ray-attacks
  (is (= [e3 e4 e5 e6] (scan (ray-attacks e2 :N (set-sqr e6)))))
  (is (= [d2 d3 d4 d5 d6] (scan (ray-attacks d7 :S (set-sqr d2)))))
  (is (= [b2 c3 d4] (scan (ray-attacks a1 :NE (set-sqr d4)))))
  (is (= [h1 g2 f3 e4 d5 c6 b7] (scan (ray-attacks a8 :SE 0)))))

(deftest test-rook-attacks
  (is (= [e1 d2 f2 g2 e3 e4 e5 e6] (scan (rook-attacks e2 (set-sqrs [e6 d2 g2])))))
  (is (= [f1 g1 h2 h3] (scan (rook-attacks h1 (set-sqrs [h3 f1])))))
  (is (= [a1 a2 a3 a4 a5 a6 b7 c7 d7 e7 f7 g7 h7 a8] (scan (rook-attacks a7 0)))))

(deftest test-bishop-attacks
  (is (= [b2 d2 a3 e3 f4 g5 h6] (scan (bishop-attacks c1 0))))
  (is (= [d1 a2 c2 a4 c4] (scan (bishop-attacks b3 (set-sqr c4)))))
  (is (= [b2 f2 c3 e3 c5 e5 b6 f6] (scan (bishop-attacks d4 (set-sqrs [b2 f2 b6 f6]))))))

(deftest test-queen-attacks
  (is (= [b2 d2 f2 c3 d3 e3 b4 c4 e4 f4 c5 d5 e5 b6 d6 f6] (scan (queen-attacks d4 (set-sqrs [b2 d2 f2 b4 f4 b6 d6 f6]))))))