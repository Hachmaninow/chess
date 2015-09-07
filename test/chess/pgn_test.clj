(ns chess.board-test
  (:require [clojure.test :refer :all]
            [chess.pgn :refer :all]
            ))

(deftest test-to-fen-start-position
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (to-fen-board start-position))))
