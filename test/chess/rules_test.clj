(ns chess.rules-test
  (:require [clojure.test :refer :all]
            [chess.rules :refer :all]
            [chess.fen :refer :all]
            [spyscope.core :refer :all]))

(deftest test-piece-color
  (is (= :white (piece-color :P) (piece-color :B) (piece-color :N) (piece-color :R) (piece-color :Q) (piece-color :K)))
  (is (= :black (piece-color :p) (piece-color :b) (piece-color :n) (piece-color :r) (piece-color :q) (piece-color :k)))
  (is (nil? (piece-color nil))))

(deftest test-piece-type
  (is (= :K (piece-type :k)))
  (is (= :K (piece-type :K)))
  (is (= :P (piece-type :p)))
  (is (nil? (piece-type nil))))

(deftest test-colored-piece
  (is (= :K (colored-piece :white :K)))
  (is (= :n (colored-piece :black :N)))
  (is (= :k (colored-piece :black :K)))
  (is (= :p (colored-piece :black :P)))
  (is (= :r (colored-piece :black :R)))
  (is (nil? (colored-piece :black nil))))

(deftest test-opponent
  (is (= :black (opponent :white)))
  (is (= :white (opponent :black))))

(deftest test-rank
  (testing "index to rank conversion"
    (is (= 0 (rank 0)))
    (is (= 0 (rank 7)))
    (is (= 7 (rank 56)))
    (is (= 7 (rank 63)))))

(deftest test-on-rank
  (testing "white's perspective"
    (is (true? (on-rank? 0 :white (to-idx :d1))))
    (is (true? (on-rank? 1 :white (to-idx :h2))))
    (is (true? (on-rank? 6 :white (to-idx :a7)))))
  (testing "black's perspective"
    (is (true? (on-rank? 0 :black (to-idx :e8))))
    (is (true? (on-rank? 1 :black (to-idx :c7))))
    (is (true? (on-rank? 7 :black (to-idx :b1))))))

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

(deftest test-is-piece?
  (is (true? (is-piece? init-board (to-idx :h8) :black :R)))
  (is (true? (is-piece? init-board (to-idx :c8) :black :B)))
  (is (true? (is-piece? init-board (to-idx :g1) :white :N)))
  (is (false? (is-piece? init-board (to-idx :g1) :black :N))))

(deftest test-init-board
  (is (= [:R :N :B :Q :K :B :N :R :P :P :P :P :P :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r] init-board)))

(deftest test-place-pieces
  (testing "single-piece"
    (is (= "8/8/8/8/8/8/8/4K3" (board->fen (place-pieces [:K :e1])))))
  (testing "simple-position"
    (is (= "2k5/3r4/8/3n4/8/8/6Q1/6K1" (board->fen (place-pieces [:K :g1 :Q :g2 :k :c8 :r :d7 :n :d5])))))
  (testing "removing-pieces"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPP2PP/RNBQKBNR" (board->fen (place-pieces init-board [nil :e2 nil :f2])))))
  (testing "removing-not-existing-pieces"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (board->fen (place-pieces init-board [nil :e4])))))
  (testing "works with indexes as well"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPP2PP/RNBQKBNR" (board->fen (place-pieces init-board [nil (to-idx :e2) nil (to-idx :f2)])))))
  (testing "make castling"
    (is (= "8/8/8/8/8/8/8/2KR4" (board->fen (place-pieces (place-pieces [:K (to-idx :e1) :R (to-idx :a1)]) [nil 4 :K 2 nil 0 :R 3]))))))

(defn direction-square-vector [square direction]
  (map to-sqr (direction-vector-internal (to-idx square) 7 direction)))

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


;
; attacks
;

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
    (is (= #{:e4} (accessible-squares :P :e3)))
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


;
; castlings
;

(deftest test-find-castlings
  (testing "passage is free"
    (is (= '(:O-O :O-O-O) (map #(:castling %) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1]) :white))))
    (is (= '(:O-O-O) (map #(:castling %) (find-castlings (place-pieces [:K :e1 :R :a1]) :white))))
    (is (= '(:O-O :O-O-O) (map #(:castling %) (find-castlings (place-pieces [:k :e8 :r :a8 :r :h8]) :black)))))

  (testing "passage is occupied with piece"
    (is (= '() (find-castlings init-board :white)))
    (is (= '(:O-O-O) (map #(:castling %) (find-castlings (place-pieces [:k :e8 :r :a8 :r :h8 :B :g8]) :black))))
    (is (= '() (map #(:castling %) (find-castlings (place-pieces [:k :e8 :r :a8 :r :h8 :B :g8 :N :b8]) :black)))))

  (testing "the king may not pass an attacked square during castling"
    (is (= '(:O-O) (map #(:castling %) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :c8]) :white))))
    (is (= '(:O-O) (map #(:castling %) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :d8]) :white))))
    (is (= '() (map #(:castling %) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :p :e2]) :white)))))
  (testing "the king must not be in check"
    (is (= '() (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :e8]) :white)))))
  (testing "the rook may pass attacked squares"
    (is (= '(:O-O :O-O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :b8]) :white))))
    (is (= '(:O-O :O-O-O) (map #(% :castling) (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1 :r :a8]) :white))))))

(deftest test-deduce-castling-availability
  (is (= {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}} (deduce-castling-availability init-board)))
  (is (= {:white #{:O-O-O}, :black #{:O-O}} (deduce-castling-availability (place-pieces empty-board [:K :e1 :R :a1 :k :e8 :r :h8]))))
  (is (= {:white #{}, :black #{}} (deduce-castling-availability (place-pieces empty-board [:K :e2 :R :a1])))))


;
; find valid moves
;

(deftest test-find-moves
  (testing "pawn move"
    (is (= [{:piece :P :from 12 :to 20} {:piece :P :from 12 :to 28 :ep-info [20 28]}] (find-moves (place-pieces [:P :e2]) :white))))
  (testing "en-passant"
    (is (= [{:ep-capture (to-idx :c4) :piece :p :from (to-idx :d4) :to (to-idx :c3) :capture nil}] (find-moves (place-pieces [:p :d4 :P :c4 :P :d3]) :black [(to-idx :c3) (to-idx :c4)]))))
  (testing "promotion"
    (is (= [:Q :R :B :N] (map :promote-to (find-moves (place-pieces [:P :e7]) :white))))
    (is (= [:Q :R :B :N] (map :promote-to (find-moves (place-pieces [:P :e7 :n :e8 :n :f8]) :white)))))
  (testing "piece capture"
    (is (= [{:piece :N :from 63 :to 46 :capture :p}] (find-moves (place-pieces [:N :h8 :P :f7 :p :g6 :r :f8]) :white))))
  (testing "castlings"
    (is (= [{:castling :O-O, :piece :K, :from 4, :to 6, :rook-from 7, :rook-to 5} {:castling :O-O-O, :piece :K, :from 4, :to 2, :rook-from 0, :rook-to 3}] (find-castlings (place-pieces [:K :e1 :R :a1 :R :h1]) :white)))))

(deftest test-under-attack?
  (testing "unblocked-queen"
    (is (under-attack? (place-pieces [:Q :e1]) (to-idx :e8) :white))
    (is (under-attack? (place-pieces [:q :e1]) (to-idx :h4) :black))
    (is (under-attack? (place-pieces [:Q :e1]) (to-idx :f2) :white))
    (is (under-attack? (place-pieces [:Q :b4]) (to-idx :a5) :white))
    (is (false? (under-attack? (place-pieces [:Q :d1]) (to-idx :e8) :white)))
    (is (false? (under-attack? (place-pieces [:Q :d1]) (to-idx :a5) :white))))
  (testing "blocked-queen"
    (is (false? (under-attack? (place-pieces [:Q :e1 :B :e6]) (to-idx :e8) :white)))
    (is (false? (under-attack? (place-pieces [:Q :e1 :N :g3]) (to-idx :h4) :white))))
  (testing "unblocked-rook"
    (is (under-attack? (place-pieces [:R :e1]) (to-idx :e8) :white))
    (is (under-attack? (place-pieces [:R :b2]) (to-idx :f2) :white))
    (is (false? (under-attack? (place-pieces [:R :f7]) (to-idx :e8) :white))))
  (testing "blocked-rook"
    (is (false? (under-attack? (place-pieces [:R :e1 :B :e6]) (to-idx :e8) :white))))
  (testing "unblocked-bishop"
    (is (under-attack? (place-pieces [:B :e1]) (to-idx :h4) :white))
    (is (under-attack? (place-pieces [:b :e1]) (to-idx :f2) :black))
    (is (false? (under-attack? (place-pieces [:B :d1]) (to-idx :a5) :white))))
  (testing "blocked-bishop"
    (is (false? (under-attack? (place-pieces [:B :e1 :b :b4]) (to-idx :a5) :white)))
    (is (false? (under-attack? (place-pieces [:b :e1 :N :g3]) (to-idx :h4) :black))))
  (testing "king"
    (is (under-attack? (place-pieces [:K :b4]) (to-idx :a5) :white))
    (is (false? (under-attack? (place-pieces [:K :a4]) (to-idx :h4) :white))))
  (testing "knight"
    (is (under-attack? (place-pieces [:N :e4]) (to-idx :f2) :white))
    (is (under-attack? (place-pieces [:n :c7]) (to-idx :a8) :black))
    (is (false? (under-attack? (place-pieces [:N :a4]) (to-idx :c6) :white)))
    (is (false? (under-attack? (place-pieces [:N :a4]) (to-idx :h6) :white))))
  (testing "pawn"
    (is (under-attack? (place-pieces [:P :e7]) (to-idx :f8) :white))
    (is (under-attack? (place-pieces [:p :a5]) (to-idx :b4) :black))
    (is (false? (under-attack? (place-pieces [:p :a5]) (to-idx :h4) :black))))
  )

(deftest test-gives-check?
  (testing "check"
    (is (gives-check? (place-pieces [:K :e1 :r :a1]) :black))
    (is (gives-check? (place-pieces [:K :c3 :n :e4]) :black))
    (is (gives-check? (place-pieces [:k :h4 :B :e1]) :white)))
  (testing "no-check"
    (is (nil? (gives-check? (place-pieces [:K :e1 :r :a2 :r :a3 :q :a4 :n :e5 :b :g7 :q :h8 :q :a8]) :black)))))

(deftest test-valid-moves
  (testing "keeps the king out of check"
    (is (= [{:piece :K, :from (to-idx :e6), :to (to-idx :e7), :capture nil}] (valid-moves (init-position (place-pieces [:K :e6 :k :e4 :r :d1 :r :f1]))))))
  (testing "puts a piece in place to prevent check"
    (is (= [{:piece :B, :from (to-idx :g1), :to (to-idx :a7), :capture nil}] (valid-moves (init-position (place-pieces [:K :a8 :B :g1 :r :a1 :r :b1]))))))
  (testing "captures a piece to prevent check"
    (is (= [{:piece :B, :from (to-idx :g7), :to (to-idx :a1), :capture :r}] (valid-moves (init-position (place-pieces [:K :a8 :B :g7 :r :a1 :r :b1])))))
    (is (= [{:piece :K, :from (to-idx :a8), :to (to-idx :b8), :capture :q}] (valid-moves (init-position (place-pieces [:K :a8 :q :b8]))))))
  (testing "castlings with availability"
    (is (= [{:castling :O-O-O, :piece :K, :from 4, :to 2, :rook-from 0, :rook-to 3}] (filter :castling (valid-moves (init-position (place-pieces [:K :e1 :R :a1]))))))
    (is (= [{:castling :O-O, :piece :K, :from 4, :to 6, :rook-from 7, :rook-to 5}] (filter :castling (valid-moves (init-position (place-pieces [:K :e1 :R :h1])))))))
  (testing "castling without availability"
    (is (= [] (filter :castling (valid-moves (init-position (place-pieces [:K :e1 :R :a1 :R :h1]) {:castling-availability {:white #{}}})))))
    (is (= [{:castling :O-O-O, :piece :k, :from 60, :to 58, :rook-from 56, :rook-to 59}] (filter :castling (valid-moves (init-position (place-pieces [:k :e8 :r :a8 :r :h8]) {:turn :black :castling-availability {:black #{:O-O-O}}})))))
    (is (= 2 (count (filter :castling (valid-moves (init-position (place-pieces [:k :e8 :r :a8 :r :h8]) {:turn :black :castling-availability {:black #{:O-O-O :O-O}}})))))))
  (testing "promotions"
    (is (= {:piece :P :from 48 :to 56 :promote-to :Q} (first (filter :promote-to (valid-moves (init-position (place-pieces [:P :a7 :N :b8])))))))
    (is (= {:piece :P :from 49 :to 56 :capture :n :promote-to :Q} (first (filter :promote-to (valid-moves (init-position (place-pieces [:P :b7 :n :a8 :N :b8]))))))))
  (testing "en-passant"
    (is (= [] (valid-moves (init-position (place-pieces [:P :e5 :p :d5 :p :e6])))))
    (is (= [{:piece :P :from 36 :to 43 :capture nil :ep-capture 35}] (valid-moves (init-position (place-pieces [:P :e5 :p :d5 :p :e6]) {:ep-info [(to-idx :d6) (to-idx :d5)]})))))
  (testing "no valid moves"
    (is (= [] (valid-moves (init-position (place-pieces [:K :a8 :q :b6])))))))

;
; position
;

(deftest test-init-position
  (testing "parameter-less version"
    (is (= init-board (:board (init-position))))
    (is (= :white (:turn (init-position)))))
  (testing "explicit board"
    (is (= "8/3k4/8/8/8/8/8/6K1" (board->fen (:board (init-position (place-pieces [:K :g1 :k :d7])))))))
  (testing "options"
    (is (= :black (:turn (init-position (place-pieces [:K :e1 :R :a1 :R :h1]) {:turn :black}))))
    (is (= {:white #{:O-O}} (:castling-availability (init-position (place-pieces [:K :e1 :R :a1 :R :h1]) {:castling-availability {:white #{:O-O}}}))))))


(deftest test-calls
  (testing "no call"
    (are [position call] (= call (:call position))
      (init-position) nil
      (play-line (init-position) :d4 :c5 :dxc5 :Qa5) :check
      (play-line (init-position) :f3 :e5 :g4 :Qh4) :checkmate
      (play-line (init-position) :c4 :d5 :Qb3 :Bh3 :gxh3 :f5 :Qxb7 :Kf7 :Qxa7 :Kg6 :f3 :c5 :Qxe7 :Rxa2 :Kf2 :Rxb2 :Qxg7+ :Kh5 :Qxg8 :Rxb1 :Rxb1 :Kh4 :Qxh8 :h5 :Qh6 :Bxh6 :Rxb8 :Be3+ :dxe3 :Qxb8 :Kg2 :Qf4 :exf4 :d4 :Be3 :dxe3) :stalemate)))

(defn play-move-on-board
  "Setup a board with the given piece-positions, then make the given move and return a FEN-representation of the board."
  ([piece-positions move]
   (let [game (init-position (place-pieces piece-positions))]
     (board->fen (:board (update-position game move)))))
  ([piece-positions game-options move]
   (let [game (init-position (place-pieces piece-positions) game-options)]
     (board->fen (:board (update-position game move)))))
  )

(deftest test-update-position-board
  (testing "updates piece positions"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1 :n :d1] {:from (to-idx :e1) :to (to-idx :d1)})))
  (testing "updates piece positions after castling"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1 :R :a1] {:from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1) :castling :O-O-O})))
  (testing "handles pawn-promotions"
    (is (= "B7/8/8/8/8/8/8/8" (play-move-on-board [:P :b7 :r :a8] {:piece :P :from 49 :to 56 :capture :r :promote-to :B}))))
  (testing "en-passant-capture clears the captured-pawn"
    (is (= "8/8/5P2/8/8/8/8/8" (play-move-on-board [:P :e5 :p :f5] {:ep-info [(to-idx :f6) (to-idx :f5)]} {:from (to-idx :e5) :to (to-idx :f6) :ep-capture (to-idx :f5)})))))

(deftest test-update-position-castling-availability
  (testing "updates castling-availability after own kings- or rook-move"
    (let [game (init-position (place-pieces [:K :e1 :R :a1 :R :h1]))]
      (is (= #{} (get-in (update-position game {:from (to-idx :e1) :to (to-idx :d1)}) [:castling-availability :white])))
      (is (= #{:O-O} (get-in (update-position game {:from (to-idx :a1) :to (to-idx :b1)}) [:castling-availability :white])))
      (is (= #{:O-O-O} (get-in (update-position game {:from (to-idx :h1) :to (to-idx :h8)}) [:castling-availability :white]))))))

(deftest test-update-position-ep-info
  (testing "double-step pawn moves are potential en-passant targets"
    (is (= [16 24] (:ep-info (update-position (init-position (place-pieces [:P :a2])) {:from (to-idx :a2) :to (to-idx :a4) :ep-info [(to-idx :a3) (to-idx :a4)]}))))))


;
; move selection
;

(deftest test-select-move
  (testing "unambiguous valid move"
    (is (= {:piece :P, :from 12, :to 20} (select-move (init-position) {:to :e3})))
    (is (= {:piece :P, :from 12, :to 28, :ep-info [20 28]} (select-move (init-position) {:to :e4})))
    (is (= {:piece :N :from 6 :to 21 :capture nil} (select-move (init-position) {:piece :N :to :f3}))))
  (testing "invalid move"
    (is (thrown-with-msg? IllegalArgumentException #"No matching moves" (select-move (init-position) {:piece :N :to :f4}))))
  (testing "invalid move"
    (is (thrown-with-msg? IllegalArgumentException #"Multiple matching moves" (select-move (init-position (place-pieces [:N :e2 :N :g2])) {:piece :N :to :f4})))))



;
; move to string
;

(deftest test-move->str
  (is (= "O-O" (move->str {:piece :K :castling :O-O})))
  (is (= "O-O-O" (move->str {:piece :k :castling :O-O-O})))
  (is (= "a2-a3" (move->str {:piece :P :from (to-idx :a2) :to (to-idx :a3)})))
  (is (= "e2-e1=N" (move->str {:piece :p :from (to-idx :e2) :to (to-idx :e1) :promote-to :n})))
  (is (= "d6xe6ep" (move->str {:piece :P :from (to-idx :d6) :to (to-idx :e6) :ep-capture (to-idx :e5)})))
  (is (= "Nf7xh8" (move->str {:piece :N :from (to-idx :f7) :to (to-idx :h8) :capture :r}))))

