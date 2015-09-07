(ns chess.game-test
  (:require [clojure.test :refer :all]
            [chess.board :refer :all]
            [chess.game :refer :all]))

(deftest test-guess-castling-rights
  (is (= {:white #{:0-0 :0-0-0} :black #{:0-0 :0-0-0}} (guess-castling-rights start-position)))
  (is (= {:white #{:0-0-0} :black #{:0-0}} (guess-castling-rights (place-pieces empty-board [:K :e1 :R :a1 :k :e8 :r :h8]))))
  (is (= {:white #{} :black #{}} (guess-castling-rights (place-pieces empty-board [:K :e2 :R :a1])))))

(deftest test-setup
  (is (= start-position ((setup) :board)))
  (is (= :white ((setup) :player-to-move))))

(deftest test-switch-player
  (is (= :black ((switch-player (setup)) :player-to-move)))
  (is (= :white ((switch-player (switch-player (setup))) :player-to-move))))


