(ns chess.game-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [chess.game :refer :all]))

(deftest test-deduce-castling-availability
  (is (= {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}} (deduce-castling-availability init-board)))
  (is (= {:white #{:O-O-O}, :black #{:O-O}} (deduce-castling-availability (place-pieces empty-board [:K :e1 :R :a1 :k :e8 :r :h8]))))
  (is (= {:white #{}, :black #{}} (deduce-castling-availability (place-pieces empty-board [:K :e2 :R :a1])))))

(deftest test-new-game
  (testing "parameter-less version"
    (is (= init-board (:board (new-game))))
    (is (= :white (:turn (new-game)))))
  (testing "explicit board"
    (is (= "8/3k4/8/8/8/8/8/6K1" (board->fen (:board (new-game (place-pieces [:K :g1 :k :d7])))))))
  (testing "options"
    (is (= :black (:turn (new-game (place-pieces [:K :e1 :R :a1 :R :h1]) {:turn :black}))))
    (is (= {:white #{:O-O}} (:castling-availability (new-game (place-pieces [:K :e1 :R :a1 :R :h1]) {:castling-availability {:white #{:O-O}}}))))))

(defn play-move-on-board
  "Setup a board with the given piece-positions, then make the given move and return a FEN-representation of the board."
  ([piece-positions move]
   (let [game (new-game (place-pieces piece-positions))]
     (board->fen (:board (play-move game move)))))
  ([piece-positions game-options move]
   (let [game (new-game (place-pieces piece-positions) game-options)]
     (board->fen (:board (play-move game move)))))
  )

(deftest test-valid-moves
  (testing "keeps the king out of check"
    (is (= [{:piece :K, :from (to-idx :e6), :to (to-idx :e7), :capture nil}] (valid-moves (new-game (place-pieces [:K :e6 :k :e4 :r :d1 :r :f1]))))))
  (testing "puts a piece in place to prevent check"
    (is (= [{:piece :B, :from (to-idx :g1), :to (to-idx :a7), :capture nil}] (valid-moves (new-game (place-pieces [:K :a8 :B :g1 :r :a1 :r :b1]))))))
  (testing "captures a piece to prevent check"
    (is (= [{:piece :B, :from (to-idx :g7), :to (to-idx :a1), :capture :r}] (valid-moves (new-game (place-pieces [:K :a8 :B :g7 :r :a1 :r :b1])))))
    (is (= [{:piece :K, :from (to-idx :a8), :to (to-idx :b8), :capture :q}] (valid-moves (new-game (place-pieces [:K :a8 :q :b8]))))))
  (testing "castlings with availability"
    (is (= [{:castling :O-O-O, :piece :K, :from 4, :to 2, :rook-from 0, :rook-to 3}] (filter :castling (valid-moves (new-game (place-pieces [:K :e1 :R :a1]))))))
    (is (= [{:castling :O-O, :piece :K, :from 4, :to 6, :rook-from 7, :rook-to 5}] (filter :castling (valid-moves (new-game (place-pieces [:K :e1 :R :h1])))))))
  (testing "castling without availability"
    (is (= [] (filter :castling (valid-moves (new-game (place-pieces [:K :e1 :R :a1 :R :h1]) {:castling-availability {:white #{}}})))))
    (is (= [{:castling :O-O-O, :piece :k, :from 60, :to 58, :rook-from 56, :rook-to 59}] (filter :castling (valid-moves (new-game (place-pieces [:k :e8 :r :a8 :r :h8]) {:turn :black :castling-availability {:black #{:O-O-O}}})))))
    (is (= 2 (count (filter :castling (valid-moves (new-game (place-pieces [:k :e8 :r :a8 :r :h8]) {:turn :black :castling-availability {:black #{:O-O-O :O-O}}})))))))
  (testing "en-passant"
    (is (= [] (valid-moves (new-game (place-pieces [:P :e5 :p :d5 :p :e6])))))
    (is (= [{:piece :P :from 36 :to 43 :capture nil :ep-capture 35}] (valid-moves (new-game (place-pieces [:P :e5 :p :d5 :p :e6]) {:ep-info [(to-idx :d6) (to-idx :d5)]})))))
  (testing "no valid moves"
    (is (= [] (valid-moves (new-game (place-pieces [:K :a8 :q :b6])))))))


(deftest test-play-move
  (testing "make-move updates piece positions"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1 :n :d1] {:from (to-idx :e1) :to (to-idx :d1)})))
  (testing "make-castling"
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1] {:from (to-idx :e1) :to (to-idx :d1)}))
    (is (= "4k3/8/8/8/8/8/5P2/3K4") (play-move-on-board [:K :e1 :R :a1] {:from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1) :castling :O-O-O})))
  (testing "updates castling-availability after own kings- or rook-move"
    (let [game (new-game (place-pieces [:K :e1 :R :a1 :R :h1]))]
      (is (= #{} (get-in (play-move game {:from (to-idx :e1) :to (to-idx :d1)}) [:castling-availability :white])))
      (is (= #{:O-O} (get-in (play-move game {:from (to-idx :a1) :to (to-idx :b1)}) [:castling-availability :white])))
      (is (= #{:O-O-O} (get-in (play-move game {:from (to-idx :h1) :to (to-idx :h8)}) [:castling-availability :white])))))
  (testing "updates castling-availability after opponent's capture"
    (let [game (new-game (place-pieces [:K :e1 :R :a1 :R :h1 :b :b7 :q :a8]) {:turn :black})]
      (is (= #{:O-O} (get-in (play-move game {:from (to-idx :a8) :to (to-idx :a1) :capture :R}) [:castling-availability :white])))
      (is (= #{:O-O-O} (get-in (play-move game {:from (to-idx :b7) :to (to-idx :h1) :capture :R}) [:castling-availability :white])))))
  (testing "double-step pawn moves are potential en-passant targets"
    (is (= [16 24] (:ep-info (play-move (new-game (place-pieces [:P :a2])) {:from (to-idx :a2) :to (to-idx :a4) :ep-info [(to-idx :a3) (to-idx :a4)]})))))
  (testing "en-passant-capture clears the captured-pawn"
    (is (= "8/8/5P2/8/8/8/8/8" (play-move-on-board [:P :e5 :p :f5] {:ep-info [(to-idx :f6) (to-idx :f5)]} {:from (to-idx :e5) :to (to-idx :f6) :ep-capture (to-idx :f5)})))))

(deftest test-select-move
  (testing "unambiguous valid move"
    (is (= {:piece :P, :from 12, :to 20} (select-move (new-game) (parse-move "e3"))))
    (is (= {:piece :P, :from 12, :to 28, :ep-info [20 28]} (select-move (new-game) (parse-move "e4"))))
    (is (= {:piece :N :from 6 :to 21 :capture nil} (select-move (new-game) (parse-move "Nf3")))))
  (testing "invalid move"
    (is (thrown-with-msg? IllegalArgumentException #"No matching moves" (select-move (new-game) (parse-move "Nf5"))))
    )
  (testing "invalid move"
    (is (thrown-with-msg? IllegalArgumentException #"Multiple matching moves" (select-move (new-game (place-pieces [:N :e2 :N :g2])) (parse-move "Nf4"))))))

(deftest test-call
  (testing "simple-move"
    (is (nil? (:call (play (new-game) "d4")))))
  (testing "with capture"
    (is (nil? (:call (play (new-game) "d4 c5 dxc5")))))
  (testing "check is called"
    (is :check (:call (play (new-game) "d4 c5 dxc5 Qa5"))))
  (testing "checkmate is called"
    (is :checkmate (:call (play (new-game) "f3 e5 g4 Qh4"))))
  (testing "statemate is called"
    (is :stalemate (:call (play (new-game) "1.c4 d5 2.Qb3 Bh3 3.gxh3 f5 4.Qxb7 Kf7 5.Qxa7 Kg6 6.f3 c5 7.Qxe7 Rxa2 8.Kf2 Rxb2 9.Qxg7+ Kh5 10.Qxg8 Rxb1 11.Rxb1 Kh4 12.Qxh8 h5 13.Qh6 Bxh6 14.Rxb8 Be3+ 15.dxe3 Qxb8 16.Kg2 Qf4 17.exf4 d4 18.Be3 dxe3")))))

(deftest test-en-passant
  (testing "simple-move"
    (is (= "rnbqkbnr/pp1ppp1p/2P3p1/8/8/8/PPP1PPPP/RNBQKBNR" (board->fen (:board (play (new-game) "d4 g6 d5 c5 dxc6")))))))