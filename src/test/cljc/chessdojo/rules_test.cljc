(ns chessdojo.rules-test
  #?(:clj
     (:require [clojure.test :refer :all]
               [chessdojo.rules :as cr :refer [to-idx to-sqr]]
               [chessdojo.fen :as cf]))
  #?(:cljs
     (:require [cljs.test :refer-macros [deftest is testing run-tests]]
       [chessdojo.rules :as cr :refer [to-idx to-sqr]]
       [chessdojo.fen :as cf])))

(deftest test-piece-color
  (is (= :white (cr/piece-color :P) (cr/piece-color :B) (cr/piece-color :N) (cr/piece-color :R) (cr/piece-color :Q) (cr/piece-color :K)))
  (is (= :black (cr/piece-color :p) (cr/piece-color :b) (cr/piece-color :n) (cr/piece-color :r) (cr/piece-color :q) (cr/piece-color :k)))
  (is (nil? (cr/piece-color nil))))

(deftest test-piece-type
  (is (= :K (cr/piece-type :k)))
  (is (= :K (cr/piece-type :K)))
  (is (= :P (cr/piece-type :p)))
  (is (nil? (cr/piece-type nil))))

(deftest test-colored-piece
  (is (= :K (cr/colored-piece :white :K)))
  (is (= :n (cr/colored-piece :black :N)))
  (is (= :k (cr/colored-piece :black :K)))
  (is (= :p (cr/colored-piece :black :P)))
  (is (= :r (cr/colored-piece :black :R)))
  (is (nil? (cr/colored-piece :black nil))))

(deftest test-opponent
  (is (= :black (cr/opponent :white)))
  (is (= :white (cr/opponent :black))))

(deftest test-rank
  (testing "index to rank conversion"
    (is (= 0 (cr/rank 0)))
    (is (= 0 (cr/rank 7)))
    (is (= 7 (cr/rank 56)))
    (is (= 7 (cr/rank 63)))))

(deftest test-file
  (testing "index to file conversion"
    (is (= 0 (cr/file 0)))
    (is (= 7 (cr/file 7)))
    (is (= 4 (cr/file 12)))
    (is (= 4 (cr/file 20)))
    (is (= 7 (cr/file 63)))))

(deftest test-to-idx
  (testing "square-to-idx conversion"
    (is (= 0 (cr/to-idx :a1)))
    (is (= 7 (cr/to-idx :h1)))
    (is (= 8 (cr/to-idx :a2)))
    (is (= 11 (cr/to-idx :d2)))
    (is (= 54 (cr/to-idx :g7)))
    (is (= 63 (cr/to-idx :h8)))))

(deftest test-on-rank
  (testing "white's perspective"
    (is (true? (cr/on-rank? 0 :white (cr/to-idx :d1))))
    (is (true? (cr/on-rank? 1 :white (cr/to-idx :h2))))
    (is (true? (cr/on-rank? 6 :white (cr/to-idx :a7)))))
  (testing "black's perspective"
    (is (true? (cr/on-rank? 0 :black (cr/to-idx :e8))))
    (is (true? (cr/on-rank? 1 :black (cr/to-idx :c7))))
    (is (true? (cr/on-rank? 7 :black (cr/to-idx :b1))))))

(deftest test-to-sqr
  (testing "index-to-sqr conversion"
    (is (= :a1 (cr/to-sqr 0)))
    (is (= :a5 (cr/to-sqr 32)))
    (is (= :h8 (cr/to-sqr 63)))))

(deftest test-distance
  (testing "neighboring-squares"
    (is (= 1 (cr/distance 10 11)))
    (is (= 1 (cr/distance 10 18)))
    (is (= 1 (cr/distance 10 3))))
  (testing "neighboring-squares"
    (is (= 7 (cr/distance 7 8)))
    (is (= 7 (cr/distance 0 63))))
  (testing "same-square"
    (is (= 0 (cr/distance 42 42)))))

(deftest test-indexes-between
  (is (= '(4 5 6) (cr/indexes-between (to-idx :e1) (to-idx :g1))))
  (is (= '(4 5 6) (cr/indexes-between (to-idx :g1) (to-idx :e1))))
  (is (= '(5 6 7) (cr/indexes-between (to-idx :h1) (to-idx :f1)))))

(deftest test-is-piece?
  (is (true? (cr/is-piece? (:board cr/start-position) (to-idx :h8) :black :R)))
  (is (true? (cr/is-piece? (:board cr/start-position) (to-idx :c8) :black :B)))
  (is (true? (cr/is-piece? (:board cr/start-position) (to-idx :g1) :white :N)))
  (is (false? (cr/is-piece? (:board cr/start-position) (to-idx :g1) :black :N))))

(deftest test-start-position
  (is (= [:R :N :B :Q :K :B :N :R :P :P :P :P :P :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r] (:board cr/start-position))))

(deftest test-place-pieces
  (testing "single-piece"
    (is (= "8/8/8/8/8/8/8/4K3" (cf/board->fen (cr/place-pieces [:K :e1])))))
  (testing "simple-position"
    (is (= "2k5/3r4/8/3n4/8/8/6Q1/6K1" (cf/board->fen (cr/place-pieces [:K :g1 :Q :g2 :k :c8 :r :d7 :n :d5])))))
  (testing "removing-pieces"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPP2PP/RNBQKBNR" (cf/board->fen (cr/place-pieces (:board cr/start-position) [nil :e2 nil :f2])))))
  (testing "removing-not-existing-pieces"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (cf/board->fen (cr/place-pieces (:board cr/start-position) [nil :e4])))))
  (testing "works with indexes as well"
    (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPP2PP/RNBQKBNR" (cf/board->fen (cr/place-pieces (:board cr/start-position) [nil (to-idx :e2) nil (to-idx :f2)])))))
  (testing "make castling"
    (is (= "8/8/8/8/8/8/8/2KR4" (cf/board->fen (cr/place-pieces (cr/place-pieces [:K (to-idx :e1) :R (to-idx :a1)]) [nil 4 :K 2 nil 0 :R 3]))))))

(defn direction-square-vector [square direction]
  (map to-sqr (cr/direction-vector-internal (to-idx square) 7 direction)))

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
    (is (= :R (cr/lookup (:board cr/start-position) :a1) (cr/lookup (:board cr/start-position) :h1))))
  (testing "black knights"
    (is (= :n (cr/lookup (:board cr/start-position) :b8) (cr/lookup (:board cr/start-position) :g8)))))

(deftest test-occupied-indexes
  (testing "occupied by white"
    (is (= (range 0 16) (cr/occupied-indexes (:board cr/start-position) :white))))
  (testing "occupied by black"
    (is (= (range 48 64) (cr/occupied-indexes (:board cr/start-position) :black)))))

(deftest test-empty-square
  (testing "empty square"
    (is (true? (cr/empty-square? (:board cr/start-position) 30))))
  (testing "non-empty square"
    (is (false? (cr/empty-square? (:board cr/start-position) 58)))))


;
; attacks
;

(defn accessible-squares
  "Find all valid target squares for the specified piece being located on the specified square on
  a board together with the specified additional pieces."
  ([piece square] (accessible-squares piece square nil))
  ([piece square additional-pieces]
   (let [board (cr/place-pieces (cr/place-piece cr/empty-board [piece square]) additional-pieces)
         turn (cr/piece-color piece)
         all-valid-moves (cr/find-moves board turn)
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
    (is (= '(:O-O :O-O-O) (map #(:castling %) (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1 :R :h1]) :white))))
    (is (= '(:O-O-O) (map #(:castling %) (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1]) :white))))
    (is (= '(:O-O :O-O-O) (map #(:castling %) (cr/find-castlings (cr/place-pieces [:k :e8 :r :a8 :r :h8]) :black)))))

  (testing "passage is occupied with piece"
    (is (= '() (cr/find-castlings (:board cr/start-position) :white)))
    (is (= '(:O-O-O) (map #(:castling %) (cr/find-castlings (cr/place-pieces [:k :e8 :r :a8 :r :h8 :B :g8]) :black))))
    (is (= '() (map #(:castling %) (cr/find-castlings (cr/place-pieces [:k :e8 :r :a8 :r :h8 :B :g8 :N :b8]) :black)))))

  (testing "the king may not pass an attacked square during castling"
    (is (= '(:O-O) (map #(:castling %) (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1 :R :h1 :r :c8]) :white))))
    (is (= '(:O-O) (map #(:castling %) (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1 :R :h1 :r :d8]) :white))))
    (is (= '() (map #(:castling %) (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1 :R :h1 :p :e2]) :white)))))
  (testing "the king must not be in check"
    (is (= '() (map #(% :castling) (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1 :R :h1 :r :e8]) :white)))))
  (testing "the rook may pass attacked squares"
    (is (= '(:O-O :O-O-O) (map #(% :castling) (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1 :R :h1 :r :b8]) :white))))
    (is (= '(:O-O :O-O-O) (map #(% :castling) (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1 :R :h1 :r :a8]) :white))))))

(deftest test-deduce-castling-availability
  (is (= {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}} (cr/deduce-castling-availability (:board cr/start-position))))
  (is (= {:white #{:O-O-O}, :black #{:O-O}} (cr/deduce-castling-availability (cr/place-pieces cr/empty-board [:K :e1 :R :a1 :k :e8 :r :h8]))))
  (is (= {:white #{}, :black #{}} (cr/deduce-castling-availability (cr/place-pieces cr/empty-board [:K :e2 :R :a1])))))


;
; find valid moves
;

(deftest test-find-moves
  (testing "pawn move"
    (is (= [{:piece :P :from 12 :to 20} {:piece :P :from 12 :to 28 :ep-info [20 28]}] (cr/find-moves (cr/place-pieces [:P :e2]) :white))))
  (testing "en-passant"
    (is (= [{:ep-capture (to-idx :c4) :piece :p :from (to-idx :d4) :to (to-idx :c3) :capture nil}] (cr/find-moves (cr/place-pieces [:p :d4 :P :c4 :P :d3]) :black [(to-idx :c3) (to-idx :c4)] nil))))
  (testing "promotion"
    (is (= [:Q :R :B :N] (map :promote-to (cr/find-moves (cr/place-pieces [:P :e7]) :white))))
    (is (= [:Q :R :B :N] (map :promote-to (cr/find-moves (cr/place-pieces [:P :e7 :n :e8 :n :f8]) :white)))))
  (testing "piece capture"
    (is (= [{:piece :N :from 63 :to 46 :capture :p}] (cr/find-moves (cr/place-pieces [:N :h8 :P :f7 :p :g6 :r :f8]) :white)))))

(deftest test-find-castling-moves
  (is (= [{:castling :O-O, :piece :K, :from 4, :to 6, :rook-from 7, :rook-to 5} {:castling :O-O-O, :piece :K, :from 4, :to 2, :rook-from 0, :rook-to 3}] (cr/find-castlings (cr/place-pieces [:K :e1 :R :a1 :R :h1]) :white))))

(deftest test-under-attack?
  (testing "unblocked-queen"
    (is (cr/under-attack? (cr/place-pieces [:Q :e1]) (to-idx :e8) :white))
    (is (cr/under-attack? (cr/place-pieces [:q :e1]) (to-idx :h4) :black))
    (is (cr/under-attack? (cr/place-pieces [:Q :e1]) (to-idx :f2) :white))
    (is (cr/under-attack? (cr/place-pieces [:Q :b4]) (to-idx :a5) :white))
    (is (false? (cr/under-attack? (cr/place-pieces [:Q :d1]) (to-idx :e8) :white)))
    (is (false? (cr/under-attack? (cr/place-pieces [:Q :d1]) (to-idx :a5) :white))))
  (testing "blocked-queen"
    (is (false? (cr/under-attack? (cr/place-pieces [:Q :e1 :B :e6]) (to-idx :e8) :white)))
    (is (false? (cr/under-attack? (cr/place-pieces [:Q :e1 :N :g3]) (to-idx :h4) :white))))
  (testing "unblocked-rook"
    (is (cr/under-attack? (cr/place-pieces [:R :e1]) (to-idx :e8) :white))
    (is (cr/under-attack? (cr/place-pieces [:R :b2]) (to-idx :f2) :white))
    (is (false? (cr/under-attack? (cr/place-pieces [:R :f7]) (to-idx :e8) :white))))
  (testing "blocked-rook"
    (is (false? (cr/under-attack? (cr/place-pieces [:R :e1 :B :e6]) (to-idx :e8) :white))))
  (testing "unblocked-bishop"
    (is (cr/under-attack? (cr/place-pieces [:B :e1]) (to-idx :h4) :white))
    (is (cr/under-attack? (cr/place-pieces [:b :e1]) (to-idx :f2) :black))
    (is (false? (cr/under-attack? (cr/place-pieces [:B :d1]) (to-idx :a5) :white))))
  (testing "blocked-bishop"
    (is (false? (cr/under-attack? (cr/place-pieces [:B :e1 :b :b4]) (to-idx :a5) :white)))
    (is (false? (cr/under-attack? (cr/place-pieces [:b :e1 :N :g3]) (to-idx :h4) :black))))
  (testing "king"
    (is (cr/under-attack? (cr/place-pieces [:K :b4]) (to-idx :a5) :white))
    (is (false? (cr/under-attack? (cr/place-pieces [:K :a4]) (to-idx :h4) :white))))
  (testing "knight"
    (is (cr/under-attack? (cr/place-pieces [:N :e4]) (to-idx :f2) :white))
    (is (cr/under-attack? (cr/place-pieces [:n :c7]) (to-idx :a8) :black))
    (is (false? (cr/under-attack? (cr/place-pieces [:N :a4]) (to-idx :c6) :white)))
    (is (false? (cr/under-attack? (cr/place-pieces [:N :a4]) (to-idx :h6) :white))))
  (testing "pawn"
    (is (cr/under-attack? (cr/place-pieces [:P :e7]) (to-idx :f8) :white))
    (is (cr/under-attack? (cr/place-pieces [:p :a5]) (to-idx :b4) :black))
    (is (false? (cr/under-attack? (cr/place-pieces [:p :a5]) (to-idx :h4) :black))))
  )

(deftest test-gives-check?
  (testing "check"
    (is (cr/gives-check? (cr/place-pieces [:K :e1 :r :a1]) :black))
    (is (cr/gives-check? (cr/place-pieces [:K :c3 :n :e4]) :black))
    (is (cr/gives-check? (cr/place-pieces [:k :h4 :B :e1]) :white)))
  (testing "no-check"
    (is (nil? (cr/gives-check? (cr/place-pieces [:K :e1 :r :a2 :r :a3 :q :a4 :n :e5 :b :g7 :q :h8 :q :a8]) :black)))))

(deftest test-valid-moves
  (testing "keeps the king out of check"
    (is (= [{:piece :K, :from (to-idx :e6), :to (to-idx :e7), :capture nil}] (cr/valid-moves (cr/setup-position [:K :e6 :k :e4 :r :d1 :r :f1])))))
  (testing "puts a piece in place to prevent check"
    (is (= [{:piece :B, :from (to-idx :g1), :to (to-idx :a7), :capture nil}] (cr/valid-moves (cr/setup-position [:K :a8 :B :g1 :r :a1 :r :b1])))))
  (testing "captures a piece to prevent check"
    (is (= [{:piece :B, :from (to-idx :g7), :to (to-idx :a1), :capture :r}] (cr/valid-moves (cr/setup-position [:K :a8 :B :g7 :r :a1 :r :b1]))))
    (is (= [{:piece :K, :from (to-idx :a8), :to (to-idx :b8), :capture :q}] (cr/valid-moves (cr/setup-position [:K :a8 :q :b8])))))
  (testing "castlings with availability"
    (is (= [{:castling :O-O-O, :piece :K, :from 4, :to 2, :rook-from 0, :rook-to 3}] (filter :castling (cr/valid-moves (cr/setup-position [:K :e1 :R :a1])))))
    (is (= [{:castling :O-O, :piece :K, :from 4, :to 6, :rook-from 7, :rook-to 5}] (filter :castling (cr/valid-moves (cr/setup-position [:K :e1 :R :h1]))))))
  (testing "castling without availability"
    (is (= [] (filter :castling (cr/valid-moves (cr/setup-position [:K :e1 :R :a1 :R :h1] {:castling-availability {:white #{}}})))))
    (is (= [{:castling :O-O-O, :piece :k, :from 60, :to 58, :rook-from 56, :rook-to 59}] (filter :castling (cr/valid-moves (cr/setup-position [:k :e8 :r :a8 :r :h8] {:turn :black :castling-availability {:black #{:O-O-O}}})))))
    (is (= 2 (count (filter :castling (cr/valid-moves (cr/setup-position [:k :e8 :r :a8 :r :h8] {:turn :black :castling-availability {:black #{:O-O-O :O-O}}})))))))
  (testing "promotions"
    (is (= {:piece :P :from 48 :to 56 :promote-to :Q} (first (filter :promote-to (cr/valid-moves (cr/setup-position [:P :a7 :N :b8]))))))
    (is (= {:piece :P :from 49 :to 56 :capture :n :promote-to :Q} (first (filter :promote-to (cr/valid-moves (cr/setup-position [:P :b7 :n :a8 :N :b8])))))))
  (testing "en-passant"
    (is (= [] (cr/valid-moves (cr/setup-position [:P :e5 :p :d5 :p :e6]))))
    (is (= [{:piece :P :from 36 :to 43 :capture nil :ep-capture 35}] (cr/valid-moves (cr/setup-position [:P :e5 :p :d5 :p :e6] {:ep-info [(to-idx :d6) (to-idx :d5)]})))) (is (= [{:piece :P :from 36 :to 43 :capture nil :ep-capture 35}] (cr/valid-moves (cr/setup-position [:P :e5 :p :d5 :p :e6] {:ep-info [(to-idx :d6) (to-idx :d5)]})))))
  (testing "no valid moves"
    (is (= [] (cr/valid-moves (cr/setup-position [:K :a8 :q :b6]))))))

;
; position
;

(deftest test-setup-position
  (testing "parameter-less version"
    (is (= (:board cr/start-position) (:board cr/start-position)))
    (is (= :white (:turn cr/start-position))))
  (testing "explicit board"
    (is (= "8/3k4/8/8/8/8/8/6K1" (cf/board->fen (:board (cr/setup-position [:K :g1 :k :d7]))))))
  (testing "options"
    (is (= :black (:turn (cr/setup-position [:K :e1 :R :a1 :R :h1] {:turn :black}))))
    (is (= {:white #{:O-O}} (:castling-availability (cr/setup-position [:K :e1 :R :a1 :R :h1] {:castling-availability {:white #{:O-O}}}))))))

(defn play-move [position move-coords]
  (let [move (cr/parse-move (name move-coords))]
    (cr/update-position position (cr/select-move position move))))

(defn play-line [position & move-coords]
  (reduce play-move position move-coords))

(deftest test-calls
  (is (nil? (cr/call cr/start-position)))
  (is (nil? (cr/call (play-line cr/start-position :d4))))
  (is (= :check (cr/call (play-line cr/start-position :d4 :c5 :dxc5 :Qa5))))
  (is (= :checkmate (cr/call (play-line cr/start-position :f3 :e5 :g4 :Qh4))))
  (is (= :stalemate (cr/call (play-line cr/start-position :c4 :d5 :Qb3 :Bh3 :gxh3 :f5 :Qxb7 :Kf7 :Qxa7 :Kg6 :f3 :c5 :Qxe7 :Rxa2 :Kf2 :Rxb2 :Qxg7 :Kh5 :Qxg8 :Rxb1 :Rxb1 :Kh4 :Qxh8 :h5 :Qh6 :Bxh6 :Rxb8 :Be3 :dxe3 :Qxb8 :Kg2 :Qf4 :exf4 :d4 :Be3 :dxe3)))))

(defn play-move-on-board
  "Setup a board with the given piece-locations, then make the given move and return a FEN-representation of the board."
  ([piece-locations move]
   (let [game (cr/setup-position piece-locations)]
     (cf/board->fen (:board (cr/update-position game move)))))
  ([piece-positions game-options move]
   (let [game (cr/setup-position piece-positions game-options)]
     (cf/board->fen (:board (cr/update-position game move))))))

(deftest test-update-position-board
  (testing "updates piece positions"
    (is (= "8/8/8/8/8/8/8/3K4" (play-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)})))
    (is (= "8/8/8/8/8/8/8/3K4" (play-move-on-board [:K :e1 :n :d1] {:from (to-idx :e1) :to (to-idx :d1)}))))
  (testing "updates piece positions after castling"
    (is (= "8/8/8/8/8/8/8/2KR4" (play-move-on-board [:K :e1 :R :a1] {:from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1) :castling :O-O-O}))))
  (testing "handles pawn-promotions"
    (is (= "B7/8/8/8/8/8/8/8" (play-move-on-board [:P :b7 :r :a8] {:piece :P :from 49 :to 56 :capture :r :promote-to :B}))))
  (testing "en-passant-capture clears the captured-pawn"
    (is (= "8/8/5P2/8/8/8/8/8" (play-move-on-board [:P :e5 :p :f5] {:ep-info [(to-idx :f6) (to-idx :f5)]} {:from (to-idx :e5) :to (to-idx :f6) :ep-capture (to-idx :f5)})))))

(deftest test-update-position-castling-availability
  (testing "updates castling-availability after own kings- or rook-move"
    (let [game (cr/setup-position [:K :e1 :R :a1 :R :h1])]
      (is (= #{} (get-in (cr/update-position game {:from (to-idx :e1) :to (to-idx :d1)}) [:castling-availability :white])))
      (is (= #{:O-O} (get-in (cr/update-position game {:from (to-idx :a1) :to (to-idx :b1)}) [:castling-availability :white])))
      (is (= #{:O-O-O} (get-in (cr/update-position game {:from (to-idx :h1) :to (to-idx :h8)}) [:castling-availability :white]))))))

(deftest test-update-position-ep-info
  (testing "double-step pawn moves are potential en-passant targets"
    (is (= [16 24] (:ep-info (cr/update-position (cr/setup-position [:P :a2]) {:from (to-idx :a2) :to (to-idx :a4) :ep-info [(to-idx :a3) (to-idx :a4)]}))))))

(deftest test-update-position-ply-count
  (is (= 1 (:ply cr/start-position)))
  (is (= 2 (:ply (cr/update-position cr/start-position {:from (to-idx :d2) :to (to-idx :d4)}))))
  (is (= 3 (:ply (cr/update-position (cr/update-position cr/start-position {:from (to-idx :g1) :to (to-idx :f3)}) {:from (to-idx :b8) :to (to-idx :c6)})))))

;
; move selection
;

(deftest parse-simple-move-test
  (is (= {:piece :P :to 40} (cr/parse-move :a6)))
  (is (= {:piece :N :to 36} (cr/parse-move :Ne5)))
  (is (= {:piece :N :capture :X :to 36} (cr/parse-move :Nxe5)))
  (is (= {:piece :P :from-file 3 :capture :X :to 36} (cr/parse-move :dxe5)))
  (is (= {:piece :N :from-file 5 :to 36} (cr/parse-move :Nfe5)))
  (is (= {:piece :N :from-rank 2 :to 36} (cr/parse-move :N3e5)))
  (is (= {:piece :N :from-file 5 :capture :X :to 36} (cr/parse-move :Nfxe5)))
  (is (= {:piece :N :from-rank 2 :capture :X :to 36} (cr/parse-move :N3xe5))))

(deftest test-matches-criteria
  (testing "pawn moves"
    (is (true? (cr/matches-criteria? {:piece :P :to (to-idx :a6)} (cr/parse-move :a6))))
    (is (false? (cr/matches-criteria? {:piece :p, :to (to-idx :a6)} (cr/parse-move :a5))))
    (is (true? (cr/matches-criteria? {:piece :p, :to (to-idx :b5), :from (to-idx :a6), :capture :P} (cr/parse-move :axb5))))
    (is (false? (cr/matches-criteria? {:piece :p, :to (to-idx :b5), :from (to-idx :a6)} (cr/parse-move :axb5))), "capture missing")
    (is (false? (cr/matches-criteria? {:piece :p, :to (to-idx :b5), :from (to-idx :c6), :capture :P} (cr/parse-move :axb5))), "wrong file"))
  (testing "piece moves"
    (is (true? (cr/matches-criteria? {:piece :N, :to (to-idx :e7)} (cr/parse-move :Ne7))))
    (is (true? (cr/matches-criteria? {:piece :n, :to (to-idx :e7)} (cr/parse-move :Ne7))))
    (is (false? (cr/matches-criteria? {:piece :B, :to (to-idx :e7)} (cr/parse-move :Ne7))))
    (is (true? (cr/matches-criteria? {:piece :n, :to (to-idx :e7), :capture :b} (cr/parse-move :Nxe7))))
    (is (false? (cr/matches-criteria? {:piece :n, :to (to-idx :e7)} (cr/parse-move :Nxe7))), "missing capture")
    (is (true? (cr/matches-criteria? {:piece :n, :to (to-idx :e7), :from (to-idx :g6), :capture :B} (cr/parse-move :Ngxe7))))
    (is (true? (cr/matches-criteria? {:piece :n, :to (to-idx :e5), :from (to-idx :d3), :capture :B} (cr/parse-move :N3xe5))))
    (is (false? (cr/matches-criteria? {:piece :n, :to (to-idx :e7), :from (to-idx :g6), :capture :B} (cr/parse-move :Ncxe7))), "wrong file")
    (is (false? (cr/matches-criteria? {:piece :n, :to (to-idx :e5), :from (to-idx :d3), :capture :B} (cr/parse-move :N7xe5))), "wrong rank"))
  (testing "castlings"
    (is (true? (cr/matches-criteria? {:piece :K :to (to-idx :g1), :from (to-idx :e1), :castling :O-O} (cr/parse-move :O-O))))
    (is (false? (cr/matches-criteria? {:piece :K :to (to-idx :g1), :from (to-idx :e1), :castling :O-O} (cr/parse-move :O-O-O)))))
  (testing "black piece"
    (is (true? (cr/matches-criteria? {:piece :n, :from 62, :to 45, :capture nil} {:piece :n :to (to-idx :f6)})))))

(deftest test-select-move
  (testing "unambiguous valid move"
    (is (= {:piece :P :from 12 :to 20} (cr/select-move cr/start-position {:piece :P :to 20})))
    (is (= {:piece :P :from 12 :to 28 :ep-info [20 28]} (cr/select-move cr/start-position {:to 28})))
    (is (= {:piece :N :from 6 :to 21 :capture nil} (cr/select-move cr/start-position {:piece :N :to (to-idx :f3)})))
    (is (= {:piece :n :from 62 :to 45 :capture nil} (cr/select-move (play-line cr/start-position :Nf3) {:piece :n :to (to-idx :f6)}))))

  (testing "valid moves with ambiguous files"
    (is (= {:piece :N :from 12 :to 29 :capture nil :disambig-file 4} (cr/select-move (cr/setup-position [:N :e2 :N :g2]) {:piece :N :from (to-idx :e2) :to (to-idx :f4)})))
    (is (= {:piece :N :from 14 :to 29 :capture nil :disambig-file 6} (cr/select-move (cr/setup-position [:N :e2 :N :g2]) {:piece :N :from (to-idx :g2) :to (to-idx :f4)})))
    (is (= {:piece :N :from 21 :to 27 :capture nil :disambig-file 5} (cr/select-move (cr/setup-position [:N :f3 :N :b5]) {:piece :N :from (to-idx :f3) :to (to-idx :d4)}))))

  (testing "valid moves with ambiguous rank"
    (is (= {:piece :N :from 12 :to 22 :capture nil :disambig-rank 1} (cr/select-move (cr/setup-position [:N :e2 :N :e4]) {:piece :N :from (to-idx :e2) :to (to-idx :g3)})))
    (is (= {:piece :N :from 28 :to 22 :capture nil :disambig-rank 3} (cr/select-move (cr/setup-position [:N :e2 :N :e4]) {:piece :N :from (to-idx :e4) :to (to-idx :g3)}))))

  (testing "valid moves with ambiguous rank and file"
    (is (= {:piece :N :from 15 :to 21 :capture nil :disambig-square 15} (cr/select-move (cr/setup-position [:N :d2 :N :d4 :N :h2 :N :h4]) {:piece :N :from (to-idx :h2) :to (to-idx :f3)}))))

  #?(:cljs (testing "invalid move"
             (is (thrown-with-msg? ExceptionInfo #"No matching moves" (cr/select-move cr/start-position {:piece :N :to (to-idx :f4)})))
             (is (thrown-with-msg? ExceptionInfo #"Multiple matching moves" (cr/select-move (cr/setup-position [:N :e2 :N :g2]) {:piece :N :to (to-idx :f4)})))))
  #?(:clj (testing "invalid move"
            (is (thrown-with-msg? Exception #"No matching moves" (cr/select-move cr/start-position {:piece :N :to (to-idx :f4)})))
            (is (thrown-with-msg? Exception #"Multiple matching moves" (cr/select-move (cr/setup-position [:N :e2 :N :g2]) {:piece :N :to (to-idx :f4)}))))))
