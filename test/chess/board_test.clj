(ns chess.board-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.fen :refer :all]
            [spyscope.core :refer :all]))

(deftest test-piece-color
  (is (= :white (piece-color :P) (piece-color :B) (piece-color :N) (piece-color :R) (piece-color :Q) (piece-color :K)))
  (is (= :black (piece-color :p) (piece-color :b) (piece-color :n) (piece-color :r) (piece-color :q) (piece-color :k)))
  (is (nil? (piece-color nil))))

(deftest test-opponent
  (is (= :black (opponent :white)))
  (is (= :white (opponent :black))))

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

(deftest test-to-idx
  (testing "square-to-idx conversion"
    (is (= 0 (to-idx :a1)))
    (is (= 7 (to-idx :h1)))
    (is (= 8 (to-idx :a2)))
    (is (= 11 (to-idx :d2)))
    (is (= 54 (to-idx :g7)))
    (is (= 63 (to-idx :h8)))))

(deftest test-to-sqr
  (testing "index-to-sqr conversion"
    (is (= :a1 (to-sqr 0)))
    (is (= :a5 (to-sqr 32)))
    (is (= :h8 (to-sqr 63)))))

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

(deftest test-indexes-between
  (is (= '(4 5 6) (indexes-between (to-idx :e1) (to-idx :g1))))
  (is (= '(4 5 6) (indexes-between (to-idx :g1) (to-idx :e1))))
  (is (= '(5 6 7) (indexes-between (to-idx :h1) (to-idx :f1)))))

(deftest test-init-board
  (is (= [:R :N :B :Q :K :B :N :R :P :P :P :P :P :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r] init-board)))

(deftest test-place-pieces
  (testing "single-piece"
    (is (= "8/8/8/8/8/8/8/4K3" (to-fen-board (place-pieces [:K :e1])))))
  (testing "simple-position"
    (is (= "2k5/3r4/8/3n4/8/8/6Q1/6K1" (to-fen-board (place-pieces [:K :g1 :Q :g2 :k :c8 :r :d7 :n :d5])))))
  (testing "removing-pieces"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPP2PP/RNBQKBNR" (to-fen-board (place-pieces init-board [nil :e2 nil :f2])))))
  (testing "removing-not-existing-pieces"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (to-fen-board (place-pieces init-board [nil :e4])))))
  (testing "works with indexes as well"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPP2PP/RNBQKBNR" (to-fen-board (place-pieces init-board [nil (to-idx :e2) nil (to-idx :f2)])))))
  (testing "make castling"
    (is (= "8/8/8/8/8/8/8/2KR4" (to-fen-board (place-pieces (place-pieces [:K (to-idx :e1) :R (to-idx :a1)]) [nil 4 :K 2 nil 0 :R 3]))))
    ))

(defn direction-square-vector [square direction]
  (map to-sqr (direction-vector (to-idx square) 7 direction)))

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
    (is (= '() (direction-square-vector :h5 :SE)))))

(deftest test-lookup
  (testing "white rooks"
    (is (= :R (lookup init-board :a1) (lookup init-board :h1))))
  (testing "black knights"
    (is (= :n (lookup init-board :b8) (lookup init-board :g8)))))

(deftest test-occupied-indexes
  (testing "occupied by white"
    (is (= (set (range 0 16)) (occupied-indexes init-board :white))))
  (testing "occupied by black"
    (is (= (set (range 48 64)) (occupied-indexes init-board :black)))))

(deftest test-empty-square
  (testing "empty square"
    (is (true? (empty-square? init-board 30))))
  (testing "non-empty square"
    (is (false? (empty-square? init-board 58)))))

(defn accessible-squares
  "Find all valid target squares for the specified piece being located on the specified square on
  a board together with the specified additional pieces."
  ([piece square] (accessible-squares piece square nil))
  ([piece square additional-pieces]
   (let [board (place-pieces (place-piece empty-board [piece square]) additional-pieces)
         turn (piece-color piece)
         all-valid-moves (find-moves board turn)
         moves-from-idx (filter #(= (to-idx square) (% :from)) all-valid-moves)]
     (set (map #(to-sqr (% :to)) moves-from-idx)))))

(deftest test-find-moves-on-empty-board
  (testing "king on empty board"
    (is (= #{:d5 :e4 :d3 :c4 :e5 :e3 :c3 :c5} (accessible-squares :K :d4)))
    (is (= #{:a2 :b1 :b2} (accessible-squares :k :a1))))
  (testing "queen on empty board"
    (is (= #{:d5 :d6 :d7 :d8 :d3 :d2 :d1 :c4 :b4 :a4 :e4 :f4 :g4 :h4
             :e5 :f6 :g7 :h8 :e3 :f2 :g1 :c3 :b2 :a1 :c5 :b6 :a7} (accessible-squares :Q :d4)))
    (is (= #{:a2 :a3 :a4 :a5 :a6 :a7 :a8 :b1 :c1 :d1 :e1 :f1 :g1 :h1
             :b2 :c3 :d4 :e5 :f6 :g7 :h8} (accessible-squares :q :a1))))
  (testing "rook on empty board"
    (is (= #{:d5 :d6 :d7 :d8 :d3 :d2 :d1 :c4 :b4 :a4 :e4 :f4 :g4 :h4} (accessible-squares :R :d4)))
    (is (= #{:a2 :a3 :a4 :a5 :a6 :a7 :a8 :b1 :c1 :d1 :e1 :f1 :g1 :h1} (accessible-squares :r :a1)))
    (is (= #{:a8 :c8 :d8 :e8 :f8 :g8 :h8 :b1 :b2 :b3 :b4 :b5 :b6 :b7} (accessible-squares :r :b8))))
  (testing "bishop on empty board"
    (is (= #{:e5 :f6 :g7 :h8 :e3 :f2 :g1 :c3 :b2 :a1 :c5 :b6 :a7} (accessible-squares :B :d4)))
    (is (= #{:b2 :c3 :d4 :e5 :f6 :g7 :h8} (accessible-squares :b :a1))))
  (testing "knight on empty board"
    (is (= #{:f6 :g5 :g3 :f2 :d2 :c3 :c5 :d6} (accessible-squares :n :e4)))
    (is (= #{:g6 :f7} (accessible-squares :N :h8))))
  (testing "pawn on empty board"
    (is (= #{:e3 :e4} (accessible-squares :P :e2)))
    (is (= #{:g6 :g5} (accessible-squares :p :g7)))
    (is (= #{:g8} (accessible-squares :P :g7)))))

(deftest test-find-moves-on-non-empty-board
  (testing "king on non-empty board"
    (is (= #{:d5 :d3 :c4 :e5 :e3 :c3} (accessible-squares :K :d4 [:Q :e4 :R :c5 :b :d3])))
    (is (= #{:d1 :d2 :e2 :f2 :f1} (accessible-squares :K :e1 [:R :a1 :R :h1]))))
  (testing "queen on non-empty board"
    (is (= #{:b1 :c1 :d1 :e1} (accessible-squares :Q :a1 [:B :b2 :K :a2 :r :e1]))))
  (testing "rook on non-empty board"
    (is (= #{:d5 :d6 :d7 :d8 :c4 :b4 :a4 :e4 :f4} (accessible-squares :R :d4 [:B :d3 :q :f4]))))
  (testing "bishop on non-empty board"
    (is (= #{:e5 :f6 :g7 :h8 :e3 :f2 :g1 :c5 :b6} (accessible-squares :B :d4 [:K :c3 :r :b6]))))
  (testing "knight on non-empty board"
    (is (= #{:e6 :f5 :f3 :e2 :c2 :b3 :b5} (accessible-squares :N :d4 [:K :e3 :R :c6 :q :b3]))))
  (testing "pawn on non-empty board"
    (is (= #{:b5 :a5} (accessible-squares :P :a4 [:p :h5 :b :b5])))
    (is (= #{:a5} (accessible-squares :P :a4 [:B :b5])))
    (is (= #{:e3} (accessible-squares :P :e2 [:b :e4])))
    (is (= #{} (accessible-squares :p :h7 [:p :h6])))
    (is (= #{:d6 :f6} (accessible-squares :p :e7 [:N :e6 :Q :f6 :R :d6])))
    (is (= #{:d6 :f6 :e6 :e5} (accessible-squares :p :e7 [:N :d6 :Q :f6])))))

(deftest test-find-castlings
  (testing "passage is free"
    (is (= '(:O-O :O-O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1]) :white #{:O-O :O-O-O}))))
    (is (= '(:O-O :O-O-O) (map #(% :castling) (find-castlings (place-pieces [:k :e8 :r :a8 :r :h8]) :black #{:O-O :O-O-O}))))
    (is (= '(:O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1]) :white #{:O-O}))))
    (is (= '(:O-O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1]) :white #{:O-O-O})))))
  (testing "passage is occupied with piece"
    (is (= '() (find-castlings init-board :white #{:O-O :O-O-O})))
    (is (= '(:O-O-O) (map #(% :castling) (find-castlings (place-pieces [:k :e8 :r :a8 :r :h8 :B :g8]) :black #{:O-O :O-O-O}))))
    (is (= '() (map #(% :castling) (find-castlings (place-pieces [:k :e8 :r :a8 :r :h8 :B :g8 :N :b8]) :black #{:O-O :O-O-O})))))
  (testing "the king may not pass an attacked square during castling"
    (is (= '(:O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :c8]) :white #{:O-O :O-O-O}))))
    (is (= '(:O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :d8]) :white #{:O-O :O-O-O}))))
    (is (= '() (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :p :e2]) :white #{:O-O :O-O-O})))))
  (testing "the king must not be in check"
    (is (= '() (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :e8]) :white #{:O-O :O-O-O})))))
  (testing "the rook may pass attacked squares"
    (is (= '(:O-O :O-O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :b8]) :white #{:O-O :O-O-O}))))
    (is (= '(:O-O :O-O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :a8]) :white #{:O-O :O-O-O}))))))

(deftest test-move-structure
  (testing "pawn move"
    (is (= '({:from 12, :piece :P, :to 20} {:from 12, :piece :P, :to 28}) (find-moves (place-pieces [:P :e2]) :white))))
  (testing "piece capture"
    (is (= {:piece :N :from 63 :to 46 :capture :p} (first (find-moves (place-pieces [:N :h8 :P :f7 :p :g6 :r :f8]) :white)))))
  (testing "castlings"
    (is (= {:castling :O-O, :piece :K, :from 4, :to 6, :rook-from 7, :rook-to 5} (first (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1]) :white #{:O-O})))))
  )
