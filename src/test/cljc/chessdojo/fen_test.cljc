(ns chessdojo.fen-test
  (:require #?(:clj [clojure.test :refer :all] :cljs [cljs.test :refer-macros [deftest is testing run-tests]])
                    [chessdojo.fen :as cf]
                    [chessdojo.rules :as cr]
                    [chessdojo.game :as cg]))

;
; board to fen
;

(deftest test-board->fen
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (cf/board->fen (:board cr/start-position))))
  (is (= "8/8/8/8/8/8/6r1/8" (cf/board->fen (cr/place-piece cr/empty-board [:r :g2]))))
  (is (= "8/6K1/8/8/4B3/1r6/8/8" (cf/board->fen (cr/place-pieces cr/empty-board [:r :b3 :B :e4 :K :g7])))))

(deftest test-position->fen
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" (cf/position->fen cr/start-position)))
  (is (= "rnbqkb1r/ppp1pp1p/5n2/3p2p1/3P4/2P5/PP1KPPPP/RNBQ1BNR w kq g6 0 4" (cf/fen (cg/soak :d4 :d5 :c3 :Nf6 :Kd2 :g5))))
  (is (= "rnbq1bnr/pppkpppp/8/3p4/3P4/2P5/PP1KPPPP/RNBQ1BNR b - - 0 3" (cf/fen (cg/soak :d4 :d5 :Kd2 :Kd7 :c3)))))

;
; position to fen
;

(deftest test-castling-availability->fen
  (is (= "KQkq" (cf/castling-availability->fen (:castling-availability cr/start-position))))
  (is (= "K" (cf/castling-availability->fen (:castling-availability (cr/setup-position [:K :e1 :R :h1])))))
  (is (= "-" (cf/castling-availability->fen (:castling-availability (cr/setup-position []))))))

;
; fen to board
;

(deftest test-parse-castling-avaiability
  (is (= {:white #{:O-O :O-O-O} :black #{}} (cf/parse-castling-availability "KQ")))
  (is (= {:white #{:O-O :O-O-O} :black #{:O-O-O}} (cf/parse-castling-availability "KQq")))
  (is (= {:white #{} :black #{:O-O}} (cf/parse-castling-availability "k")))
  (is (= {:white #{} :black #{}} (cf/parse-castling-availability ""))))

(deftest test-fen->game
  (let [game (cf/fen->game "r1bq1rk1/1p1nbpp1/2p2n1p/p2p4/3P3B/2NBPN2/PPQ2PPP/R3K2R w KQ a6 3 11")]
    (is (= "r1bq1rk1/1p1nbpp1/2p2n1p/p2p4/3P3B/2NBPN2/PPQ2PPP/R3K2R" (cf/board->fen (:board game))))
    (is (= :white (:turn game)))
    (is (= {:white #{:O-O :O-O-O} :black #{}} (:castling-availability game)))
    (is (= :a6 (:ep-info game)))
    (is (= 3 (:fifty-rule-halfmove-clock game)))
    (is (= 11 (:move-no game)))))