(ns chess.fen-test
  (:require [clojure.test :refer :all]
            [chess.rules :refer :all]
            [chess.game :refer :all]
            [chess.fen :refer :all]))

;
; board to fen
;

(deftest test-board->fen
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (board->fen (:board start-position))))
  (is (= "8/8/8/8/8/8/6r1/8" (board->fen (place-piece empty-board [:r :g2]))))
  (is (= "8/6K1/8/8/4B3/1r6/8/8" (board->fen (place-pieces empty-board [:r :b3 :B :e4 :K :g7])))))

(deftest test-position->fen
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 1 1" (position->fen start-position)))
  (is (= "rnbqkb1r/ppp1pp1p/5n2/3p2p1/3P4/2P5/PP1KPPPP/RNBQ1BNR w kq g6 1 4" (position->fen (game-position (load-pgn "d4 d5 c3 Nf6 Kd2 g5")))))
  (is (= "rnbq1bnr/pppkpppp/8/3p4/3P4/2P5/PP1KPPPP/RNBQ1BNR b - - 1 3" (position->fen (game-position (load-pgn "d4 d5 Kd2 Kd7 c3")))))
  )

;
; position to fen
;

(deftest test-castling-availability->fen
  (is (= "KQkq" (castling-availability->fen (:castling-availability start-position))))
  (is (= "K" (castling-availability->fen (:castling-availability (setup-position [:K :e1 :R :h1])))))
  (is (= "-" (castling-availability->fen (:castling-availability (setup-position [])))))
)

;
; fen to board
;

(deftest test-parse-castling-avaiability
  (is (= {:white #{:O-O :O-O-O} :black #{}} (parse-castling-availability "KQ")))
  (is (= {:white #{:O-O :O-O-O} :black #{:O-O-O}} (parse-castling-availability "KQq")))
  (is (= {:white #{} :black #{:O-O}} (parse-castling-availability "k")))
  (is (= {:white #{} :black #{}} (parse-castling-availability ""))))

(deftest test-fen->game
  (let [game (fen->game "r1bq1rk1/1p1nbpp1/2p2n1p/p2p4/3P3B/2NBPN2/PPQ2PPP/R3K2R w KQ a6 3 11")]
    (is (= "r1bq1rk1/1p1nbpp1/2p2n1p/p2p4/3P3B/2NBPN2/PPQ2PPP/R3K2R" (board->fen (:board game))))
    (is (= :white (:turn game)))
    (is (= {:white #{:O-O :O-O-O} :black #{}}  (:castling-availability game)))
    (is (= :a6 (:ep-info game)))
    (is (= 3 (:fifty-rule-halfmove-clock game)))
    (is (= 11 (:move-no game)))))