(ns chess.board-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.fen :refer :all]
            ))

(deftest test-to-index
  (testing "square-to-index conversion"
    (is (= 0 (to-index :a1)))
    (is (= 7 (to-index :h1)))
    (is (= 8 (to-index :a2)))
    (is (= 11 (to-index :d2)))
    (is (= 54 (to-index :g7)))
    (is (= 63 (to-index :h8)))))

(deftest test-to-square
  (testing "index-to-square conversion"
    (is (= :a1 (to-square 0)))
    (is (= :a5 (to-square 32)))
    (is (= :h8 (to-square 63)))))

(deftest test-rank
  (testing "index to rank conversion"
    (is (= 0 (rank 0)))
    (is (= 0 (rank 7)))
    (is (= 7 (rank 56)))
    (is (= 7 (rank 63)))))

(deftest test-file
  (testing "index to file conversion"
    (is (= 0 (file 0)))
    (is (= 7 (file 7)))
    (is (= 4 (file 12)))
    (is (= 4 (file 20)))
    (is (= 7 (file 63)))))

(deftest test-distance
  (testing "neighboring-squares"
    (is (= 1 (distance 10 11)))
    (is (= 1 (distance 10 18)))
    (is (= 1 (distance 10 3))))
  (testing "neighboring-squares"
    (is (= 7 (distance 7 8)))
    (is (= 7 (distance 0 63))))
  (testing "same-square"
    (is (= 0 (distance 42 42)))))

(defn direction-square-vector [square direction]
  (map to-square (direction-vector (to-index square) 7 direction)))

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

(deftest test-start-position
  (is (= [:R :N :B :Q :K :B :N :R :P :P :P :P :P :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r] start-position)))

(deftest test-place-piece
  (is (= "8/8/8/8/8/8/6r1/8" (to-fen-board (place-piece empty-board [:r :g2])))))

(deftest test-place-pieces
  (is (= "8/6K1/8/8/4B3/1r6/8/8" (to-fen-board (place-pieces empty-board [:r :b3 :B :e4 :K :g7])))))

(deftest test-lookup
  (testing "white rooks"
    (is (= :R (lookup start-position :a1) (lookup start-position :h1))))
  (testing "black knights"
    (is (= :n (lookup start-position :b8) (lookup start-position :g8)))))

(deftest test-piece-color
  (testing "white pieces"
    (is (= :white (piece-color :P) (piece-color :B) (piece-color :N) (piece-color :R) (piece-color :Q) (piece-color :K))))
  (testing "black pieces"
    (is (= :black (piece-color :p) (piece-color :b) (piece-color :n) (piece-color :r) (piece-color :q) (piece-color :k))))
  (testing "empty squares"
    (is (nil? (piece-color nil))))
  (testing "start position"
    (is (:white piece-color (get start-position 0)))
    (is (:black piece-color (get start-position 63)))
    ))

(deftest test-occupied-indexes
  (testing "occupied by white"
    (is (= (set (range 0 16)) (occupied-indexes start-position :white))))
  (testing "occupied by black"
    (is (= (set (range 48 64)) (occupied-indexes start-position :black)))))

(deftest test-empty-square
  (testing "empty square"
    (is (true? (empty-square? start-position 30))))
  (testing "non-empty square"
    (is (false? (empty-square? start-position 58)))))

(defn squares-in-reach
  ([piece square] (squares-in-reach piece square nil))
  ([piece square additional-pieces]
   (let [board-with-moving-piece (place-piece empty-board [piece square])
         board-with-pieces (place-pieces board-with-moving-piece additional-pieces)]
     (set
       (map to-square
            (find-moves (get-in all-pieces [piece :type]) (to-index square) board-with-pieces))))))

(deftest test-find-moves-on-empty-board
  (testing "king on empty board"
    (is (= #{:d5 :e4 :d3 :c4 :e5 :e3 :c3 :c5} (squares-in-reach :K :d4)))
    (is (= #{:a2 :b1 :b2} (squares-in-reach :k :a1))))
  (testing "queen on empty board"
    (is (= #{:d5 :d6 :d7 :d8 :d3 :d2 :d1 :c4 :b4 :a4 :e4 :f4 :g4 :h4
             :e5 :f6 :g7 :h8 :e3 :f2 :g1 :c3 :b2 :a1 :c5 :b6 :a7} (squares-in-reach :Q :d4)))
    (is (= #{:a2 :a3 :a4 :a5 :a6 :a7 :a8 :b1 :c1 :d1 :e1 :f1 :g1 :h1
             :b2 :c3 :d4 :e5 :f6 :g7 :h8} (squares-in-reach :q :a1))))
  (testing "rook on empty board"
    (is (= #{:d5 :d6 :d7 :d8 :d3 :d2 :d1 :c4 :b4 :a4 :e4 :f4 :g4 :h4} (squares-in-reach :R :d4)))
    (is (= #{:a2 :a3 :a4 :a5 :a6 :a7 :a8 :b1 :c1 :d1 :e1 :f1 :g1 :h1} (squares-in-reach :r :a1))))
  (testing "bishop on empty board"
    (is (= #{:e5 :f6 :g7 :h8 :e3 :f2 :g1 :c3 :b2 :a1 :c5 :b6 :a7} (squares-in-reach :B :d4)))
    (is (= #{:b2 :c3 :d4 :e5 :f6 :g7 :h8} (squares-in-reach :b :a1))))
  (testing "knight on empty board"
    (is (= #{:f6 :g5 :g3 :f2 :d2 :c3 :c5 :d6} (squares-in-reach :n :e4)))
    (is (= #{:g6 :f7} (squares-in-reach :N :h8))))
  (testing "pawn on empty board"
    (is (= #{:a5} (squares-in-reach :P :a4)))
    (is (= #{:e3 :e4} (squares-in-reach :P :e2)))
    (is (= #{:d1} (squares-in-reach :p :d2)))
    (is (= #{:g6 :g5} (squares-in-reach :p :g7))))
  )

(deftest test-find-moves-on-non-empty-board
  (testing "king on non-empty board"
    (is (= #{:d5 :d3 :c4 :e5 :e3 :c3} (squares-in-reach :K :d4 [:Q :e4 :R :c5 :b :d3]))))
  (testing "queen on non-empty board"
    (is (= #{:b1 :c1 :d1 :e1} (squares-in-reach :Q :a1 [:B :b2 :K :a2 :r :e1]))))
  (testing "rook on non-empty board"
    (is (= #{:d5 :d6 :d7 :d8 :c4 :b4 :a4 :e4 :f4} (squares-in-reach :R :d4 [:B :d3 :q :f4]))))
  (testing "bishop on non-empty board"
    (is (= #{:e5 :f6 :g7 :h8 :e3 :f2 :g1 :c5 :b6} (squares-in-reach :B :d4 [:K :c3 :r :b6]))))
  (testing "knight on non-empty board"
    (is (= #{:e2 :f5 :e6 :c2 :f3 :b3 :b5} (squares-in-reach :N :d4 [:K :e3 :R :c6 :q :b3]))))
  (testing "pawn on non-empty board"
    (is (= #{:a5 :b5} (squares-in-reach :P :a4 [:p :h5 :b :b5])))
    (is (= #{:d6 :f6} (squares-in-reach :p :e7 [:N :e6 :Q :f6 :R :d6])))

  ))




;(testing "rook on empty-board"
;  (is (= #{:d5 :d6 :d7 :d8 :d3 :d2 :d1 :c4 :b4 :a4 :e4 :f4 :g4 :h4} (squares-in-reach :R :d4 empty-board)))
;  (is (= #{:a2 :a3 :a4 :a5 :a6 :a7 :a8 :b1 :c1 :d1 :e1 :f1 :g1 :h1} (squares-in-reach :r :a1 empty-board))))
;(testing "bishop on empty-board"
;  (is (= #{:e5 :f6 :g7 :h8 :e3 :f2 :g1 :c3 :b2 :a1 :c5 :b6 :a7} (squares-in-reach :B :d4 empty-board)))
;  (is (= #{:b2 :c3 :d4 :e5 :f6 :g7 :h8} (squares-in-reach :b :a1 empty-board)))))

