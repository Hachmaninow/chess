(ns chess.game-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [chess.game :refer :all]))

(deftest test-deduce-castling-availability
  (is (= #{:K :Q :k :q} (deduce-castling-availability init-board)))
  (is (= #{:Q :k} (deduce-castling-availability (place-pieces empty-board [:K :e1 :R :a1 :k :e8 :r :h8]))))
  (is (= #{} (deduce-castling-availability (place-pieces empty-board [:K :e2 :R :a1])))))

(deftest test-new-game
  (is (= init-board ((new-game) :board)))
  (is (= :white ((new-game) :turn))))

(defn play-move-on-board [piece-positions move]
  "Setup a board with the given piece-positions, then make the given move and return a FEN-representation of the board."
  (let [game (new-game (place-pieces piece-positions))]
    (board->fen (:board (play-move game move)))))

(deftest test-valid-moves
  (testing "keeps the king out of check"
    (is (= [{:piece :K, :from (to-idx :e6), :to (to-idx :e7), :capture nil}] (valid-moves (new-game (place-pieces [:K :e6 :k :e4 :r :d1 :r :f1]))))))
  (testing "puts a piece in place to prevent check"
    (is (= [{:piece :B, :from (to-idx :g1), :to (to-idx :a7), :capture nil}] (valid-moves (new-game (place-pieces [:K :a8 :B :g1 :r :a1 :r :b1]))))))
  (testing "captures a piece to prevent check"
    (is (= [{:piece :B, :from (to-idx :g7), :to (to-idx :a1), :capture :r}] (valid-moves (new-game (place-pieces [:K :a8 :B :g7 :r :a1 :r :b1])))))
    (is (= [{:piece :K, :from (to-idx :a8), :to (to-idx :b8), :capture :q}] (valid-moves (new-game (place-pieces [:K :a8 :q :b8]))))))
  (testing "no valid moves"
    (is (= [] (valid-moves (new-game (place-pieces [:K :a8 :q :b6])))))))

(deftest test-play-move
  (testing "make-move updates piece positions"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1 :n :d1] {:from (to-idx :e1) :to (to-idx :d1)})))
  (testing "make-castling"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1 :R :a1] {:from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1) :castling :O-O-O})))
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

(deftest test-call
  (testing "simple-move"
    (is (nil? (:call (play (new-game) "d4"))))
    )
  (testing "normal"
    (is (nil? (:call (play (new-game) "d4 c5 dxc5"))))
    )
  (testing "check"
    (is :check (:call (play (new-game) "d4 c5 dxc5 Qa5")))
    )
  (testing "fools mate"
    (is :checkmate (:call (play (new-game) "f3 e5 g4 Qh4")))
    )
  (testing "fastest statemate"
    (is :stalemate (:call (play (new-game) "1.c4 d5 2.Qb3 Bh3 3.gxh3 f5 4.Qxb7 Kf7 5.Qxa7 Kg6 6.f3 c5 7.Qxe7 Rxa2 8.Kf2 Rxb2 9.Qxg7+ Kh5 10.Qxg8 Rxb1 11.Rxb1 Kh4 12.Qxh8 h5 13.Qh6 Bxh6 14.Rxb8 Be3+ 15.dxe3 Qxb8 16.Kg2 Qf4 17.exf4 d4 18.Be3 dxe3")))
    )
  )
