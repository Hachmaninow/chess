(ns chess.game-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [chess.game :refer :all]))

(deftest test-guess-castling-rights
  (is (= {:white #{:0-0 :0-0-0} :black #{:0-0 :0-0-0}} (guess-castling-rights init-board)))
  (is (= {:white #{:0-0-0} :black #{:0-0}} (guess-castling-rights (place-pieces empty-board [:K :e1 :R :a1 :k :e8 :r :h8]))))
  (is (= {:white #{} :black #{}} (guess-castling-rights (place-pieces empty-board [:K :e2 :R :a1])))))

(deftest test-new-game
  (is (= init-board ((new-game) :board)))
  (is (= :white ((new-game) :turn))))

(deftest test-switch-player
  (is (= :black ((switch-player (new-game)) :turn)))
  (is (= :white ((switch-player (switch-player (new-game))) :turn))))

(defn make-move-on-board [piece-positions move]
  "Setup a board with the given piece-positions, then make the given move and return a FEN-representation of the board."
  (let [game (new-game (place-pieces piece-positions))]
    (to-fen-board ((make-move game move) :board))))


(deftest test-valid-moves
  (testing "keeps the king out of check"
    (is (= [{:piece :K, :from (to-idx :e6), :to (to-idx :e7), :capture nil}] (valid-moves (new-game (place-pieces [:K :e6 :k :e4 :r :d1 :r :f1]))))))

  (testing "puts a piece in place to prevent check"
    (is (= [{:piece :B, :from (to-idx :g1), :to (to-idx :a7), :capture nil}] (valid-moves (new-game (place-pieces [:K :a8 :B :g1 :r :a1 :r :b1]))))))

  (testing "captures a piece to prevent check"
    (is (= [{:piece :B, :from (to-idx :g7), :to (to-idx :a1), :capture :r}] (valid-moves (new-game (place-pieces [:K :a8 :B :g7 :r :a1 :r :b1]))))))
    )





(deftest test-make-move
  (testing "make-move updates piece positions"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (make-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (make-move-on-board [:K :e1 :n :d1] {:from (to-idx :e1) :to (to-idx :d1)})))
  (testing "make-castling"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (make-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (make-move-on-board [:K :e1 :R :a1] {:from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1) :castling :O-O-O})))
  )

(deftest test-select-move
  (testing "unambiguous valid move"
    (is (= {:piece :P, :from 12, :to 28} (select-move (new-game) (parse-move "e4"))))
    (is (= {:piece :N, :from 6, :to 21, :capture nil} (select-move (new-game) (parse-move "Nf3")))))
  (testing "invalid move"
    (is (thrown-with-msg? IllegalArgumentException #"No matching moves" (select-move (new-game) (parse-move "Nf5"))))
    )
  (testing "invalid move"
    (is (thrown-with-msg? IllegalArgumentException #"Multiple matching moves" (select-move (new-game (place-pieces [:N :e2 :N :g2])) (parse-move "Nf4"))))))
