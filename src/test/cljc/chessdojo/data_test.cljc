(ns chessdojo.data-test
  #?(:clj
     (:require [clojure.java.io :as io]
               [clojure.zip :as zip]
               [clojure.test :refer :all]
               [chessdojo.game :as cg]
               [chessdojo.data :as cd]
               [chessdojo.fen :as cf]))
  #?(:cljs
     (:require [cljs.test :refer-macros [deftest is testing run-tests]]
       [chessdojo.data :as cd]
       [chessdojo.game :as cg]
       [chessdojo.fen :as cf]
       [clojure.zip :as zip])))

(deftest test-deflate
  (is (= "(e4 e5 (c5 Nc3 (g3 g6 Bg2 (a3 Bg7 (h5)))) Nf3)"
         (cd/deflate (cg/soak :e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5))))
  (is (= "(e4 c5 \"the sicilian defence\")"
         (cd/deflate (cg/soak {:piece :P :to 28} {:piece :P :to 34} {:comment "the sicilian defence"}))))
  (is (= "(e4 c5 (d5 \"the scandinavian defence\"))"
         (cd/deflate (cg/soak {:piece :P :to 28} {:piece :P :to 34} :back {:piece :P :to 35} {:comment "the scandinavian defence"})))))

(deftest test-load-game
  (is (= "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
         (-> (cd/load-game "(e4 e5 (c5 Nc3 (g3 g6 Bg2 (a3 Bg7 (h5)))) Nf3)") cf/fen)))
  (is (= "the sicilian defence"
         (-> (cd/load-game "(e4 c5 \"the sicilian defence\")") zip/node :comment))))

#?(:clj                                                     ; io/resource not available in cljs
   (deftest test-complete-game
     (is (= "8/Q6p/6p1/5p2/5P2/2p3P1/3r3P/2K1k3 b - - 1 44" (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp cd/load-game cf/fen)))
     ))

;(time
;  (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp cd/load-game cf/fen))
;260ms