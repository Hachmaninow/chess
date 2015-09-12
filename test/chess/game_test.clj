(ns chess.game-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.fen :refer :all]
            [chess.game :refer :all]))

(deftest test-guess-castling-rights
  (is (= {:white #{:0-0 :0-0-0} :black #{:0-0 :0-0-0}} (guess-castling-rights init-board)))
  (is (= {:white #{:0-0-0} :black #{:0-0}} (guess-castling-rights (place-pieces empty-board [:K :e1 :R :a1 :k :e8 :r :h8]))))
  (is (= {:white #{} :black #{}} (guess-castling-rights (place-pieces empty-board [:K :e2 :R :a1])))))

(deftest test-setup
  (is (= init-board ((setup) :board)))
  (is (= :white ((setup) :turn))))

(deftest test-switch-player
  (is (= :black ((switch-player (setup)) :turn)))
  (is (= :white ((switch-player (switch-player (setup))) :turn))))

(defn make-move-on-board [piece-positions move]
  "Setup a board with the given piece-positions, then make the given move and return a FEN-representation of the board."
  (let [game (setup (place-pieces piece-positions))]
    (to-fen-board ((make-move game move) :board))))

(deftest test-make-move
  (testing "make-move updates piece positions"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (make-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (make-move-on-board [:K :e1 :n :d1] {:from (to-idx :e1) :to (to-idx :d1)})))
  
  (testing "make-castling"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (make-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (make-move-on-board [:K :e1 :R :a1] {:from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1) :type :O-O-O})))
  )

