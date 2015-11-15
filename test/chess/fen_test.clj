(ns chess.fen-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.fen :refer :all]))

(deftest test-to-fen-start-position
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (board->fen init-board))))

(deftest test-to-fen-single-piece
  (is (= "8/8/8/8/8/8/6r1/8" (board->fen (place-piece empty-board [:r :g2])))))

(deftest test-to-fen-sample-position
  (is (= "8/6K1/8/8/4B3/1r6/8/8" (board->fen (place-pieces empty-board [:r :b3 :B :e4 :K :g7])))))

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