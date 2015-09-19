(ns chess.fen-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.fen :refer :all]))

(deftest test-to-fen-start-position
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (to-fen-board init-board))))

(deftest test-to-fen-single-piece
  (is (= "8/8/8/8/8/8/6r1/8" (to-fen-board (place-piece empty-board [:r :g2])))))

(deftest test-to-fen-sample-position
  (is (= "8/6K1/8/8/4B3/1r6/8/8" (to-fen-board (place-pieces empty-board [:r :b3 :B :e4 :K :g7])))))
