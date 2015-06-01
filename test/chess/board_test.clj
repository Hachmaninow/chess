(ns chess.board-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]))

(deftest test-to-index
  (testing "square-to-index conversion"
    (is (= 0 (to-index :a1)))
    (is (= 7 (to-index :h1)))
    (is (= 8 (to-index :a2)))
    (is (= 11 (to-index :d2)))
    (is (= 54 (to-index :g7)))
    (is (= 63 (to-index :h8)))
    )
  )

(deftest test-to-square
  (testing "index-to-square conversion"
    (is (= :a1 (to-square 0)))
    (is (= :a5 (to-square 32)))
    (is (= :h8 (to-square 63))))
  )

(deftest test-rank
  (testing "index to rank conversion"
    (is (= 0 (rank 0)))
    (is (= 0 (rank 7)))
    (is (= 7 (rank 56)))
    (is (= 7 (rank 63)))
    )
  )

(deftest test-file
  (testing "index to file conversion"
    (is (= 0 (file 0)))
    (is (= 7 (file 7)))
    (is (= 4 (file 12)))
    (is (= 4 (file 20)))
    (is (= 7 (file 63)))
    )
  )

(deftest test-neighboring-squares
  (testing "center square"
    (is (true? (neighboring-squares? (to-index :d4) (to-index :e4))))
    (is (false? (neighboring-squares? (to-index :d4) (to-index :f4)))))
  (testing "corner square"
    (is (true? (neighboring-squares? (to-index :a1) (to-index :b1))))
    (is (true? (neighboring-squares? (to-index :a1) (to-index :a2))))
    (is (true? (neighboring-squares? (to-index :a1) (to-index :b2))))
    (is (false? (neighboring-squares? (to-index :a1) (to-index :b3)))))
  (testing "same square"
    (is (false? (neighboring-squares? (to-index :d4) (to-index :d4)))))
  )

(defn direction-square-vector [square direction] (map to-square (direction-vector (to-index square) direction)))

(deftest test-direction-vector
  (testing "north-direction"
    (is (= '(:d2 :d3 :d4 :d5 :d6 :d7 :d8) (direction-square-vector :d1 :N)))
    (is (= '(:g5 :g6 :g7 :g8) (direction-square-vector :g4 :N)))
    (is (= '() (direction-square-vector :d8 :N))))
  (testing "south-direction"
    (is (= '(:f7 :f6 :f5 :f4 :f3 :f2 :f1) (direction-square-vector :f8 :S)))
    (is (= '(:c4 :c3 :c2 :c1) (direction-square-vector :c5 :S)))
    (is (= '() (direction-square-vector :d1 :S))))
  (testing "west-direction"
    (is (= '(:e2 :d2 :c2 :b2 :a2) (direction-square-vector :f2 :W)))
    (is (= '(:a2) (direction-square-vector :b2 :W)))
    (is (= '() (direction-square-vector :a4 :W))))
  (testing "east-direction"
    (is (= '(:f6 :g6 :h6) (direction-square-vector :e6 :E)))
    (is (= '(:b6 :c6 :d6 :e6 :f6 :g6 :h6) (direction-square-vector :a6 :E)))
    (is (= '() (direction-square-vector :h6 :E))))
  (testing "north-west-direction"
    (is (= '(:d6 :c7 :b8) (direction-square-vector :e5 :NW)))
    (is (= '() (direction-square-vector :a1 :NW))))
  (testing "south-west-direction"
    (is (= '(:d4 :c3 :b2 :a1) (direction-square-vector :e5 :SW)))
    (is (= '() (direction-square-vector :a6 :SW))))
  (testing "north-east-direction"
    (is (= '(:f6 :g7 :h8) (direction-square-vector :e5 :NE)))
    (is (= '(:g6 :h7) (direction-square-vector :f5 :NE)))
    (is (= '() (direction-square-vector :h3 :NE))))
  (testing "south-east-direction"
    (is (= '(:f4 :g3 :h2) (direction-square-vector :e5 :SE)))
    (is (= '(:f3 :g2 :h1) (direction-square-vector :e4 :SE)))
    (is (= '() (direction-square-vector :h5 :SE))))
  )

(deftest test-place-piece
  (contains? (place-piece empty-board [:R :g2]) :R)
  )

(deftest test-start-position
  (is (= [:R :N :B :Q :K :B :N :R :P :P :P :P :P :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r] start-position)))

(deftest test-lookup
  (testing "white rooks"
    (is (= :R (lookup start-position :a1) (lookup start-position :h1))))
  (testing "black knights"
    (is (= :n (lookup start-position :b8) (lookup start-position :g8))))
  )