(ns chessdojo.views.board-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [chessdojo.game :as cg]
            [chessdojo.fen :as cf]
            [chessdojo.rules :as cr]
            [chessdojo.game :as cg]
            [chessdojo.views.board :as cvb]))

(deftest test-move-destinations
  (testing "given a game the move-destinations group from squares with list of to squares"
    (is (= {:a2 [:a3 :a4]
            :b2 [:b3 :b4]
            :d2 [:d3 :d4]
            :e2 [:e3 :e4]
            :b1 [:c3 :a3]
            :g2 [:g3 :g4]
            :c2 [:c3 :c4]
            :g1 [:h3 :f3]
            :f2 [:f3 :f4]
            :h2 [:h3 :h4]} (cvb/move-destinations cg/new-game)))))