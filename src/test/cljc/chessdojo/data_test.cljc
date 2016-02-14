(ns chessdojo.data-test
  #?(:clj
     (:require [clojure.test :refer :all]
               [chessdojo.data :as cd]
               [chessdojo.game :as cg]
               [clojure.java.io :as io]
               [clojure.zip :as zip]))
  #?(:cljs
     (:require [cljs.test :refer-macros [deftest is testing run-tests]]
       [chessdojo.data :as cd]
       [chessdojo.game :as cg]
       [clojure.zip :as zip])))

(deftest test-deflate
  (is (= [[12 28] [52 36] [[50 34] [1 18] [[14 22] [54 46] [5 14] [[8 16] [61 54] [[55 39]]]]] [6 21]]
         (cd/deflate (cg/psoak [:e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5]))))

  (is (= [[12 28] [50 34 "the sicilian defence"]]
         (cd/deflate (cg/soak [{:piece :P :to 28} {:piece :P :to 34} {:comment "the sicilian defence"}])))))

(deftest test-inflate
  (is (= [{:from 12 :to 28} {:from 52 :to 36}
          [{:from 50 :to 34} {:from 1 :to 18}
           [{:from 14 :to 22} {:from 54 :to 46} {:from 5 :to 14}
            [{:from 8 :to 16} {:from 61 :to 54}
             [{:from 55 :to 39}]]]]
          {:from 6 :to 21}]
         (cd/inflate [[12 28] [52 36] [[50 34] [1 18] [[14 22] [54 46] [5 14] [[8 16] [61 54] [[55 39]]]]] [6 21]])))

  (testing "works with lists as well"
    (is (= [{:from 12 :to 28} {:from 52 :to 36}]
           (cd/inflate (list (list 12 28) (list 52 36))))))

  (testing "comments"
    (is (= [{:from 12 :to 28} {:from 50 :to 34 :comment "the sicilian defence"}]
           (cd/inflate [[12 28] [50 34 "the sicilian defence"]]))))
  )

(deftest test-deflate-inflate
  (is (= [{:from 12 :to 28} {:from 52 :to 36}
          [{:from 50 :to 34} {:from 1 :to 18}
           [{:from 14 :to 22} {:from 54 :to 46} {:from 5 :to 14}
            [{:from 8 :to 16} {:from 61 :to 54}
             [{:from 55 :to 39}]]]]
          {:from 6 :to 21}]
         (cd/inflate (cd/deflate (cg/psoak [:e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5]))))))

(deftest test-load-game
  (is (= "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R"
         (-> [:e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5] cg/psoak cd/deflate cd/load-game cg/game->board-fen) ))
  (is (= "the sicilian defence"
         (-> [[12 28] [50 34 "the sicilian defence"]] cd/load-game zip/node :comment))))

#?(:clj                                                     ; io/resource not available in cljs
   (deftest test-complete-game
     (is (= "8/Q6p/6p1/5p2/5P2/2p3P1/3r3P/2K1k3" (-> "games/deflated/complete.dgn" io/resource slurp read-string cd/load-game cg/game->board-fen)))
     (is (= "8/Q6p/6p1/5p2/5P2/2p3P1/3r3P/2K1k3" (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp read-string cd/load-game cg/game->board-fen)))))

;(time
;  (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp read-string cd/load-game cg/game->board-fen))
;530ms
