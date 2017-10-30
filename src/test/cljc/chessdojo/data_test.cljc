(ns chessdojo.data-test
  (:require
    #?(:clj [clojure.test :refer :all] :cljs [cljs.test :refer-macros [deftest is testing run-tests]])
    #?(:clj
            [clojure.java.io :as io])
            [clojure.zip :as zip]
            [chessdojo.game :as cg]
            [chessdojo.data :as cd]
            [chessdojo.fen :as cf]))

(deftest test-deflate
  (is (= "(e4 e5 (c5 Nc3 (g3 g6 Bg2 (a3 Bg7 (h5)))) Nf3)"
        (pr-str (cd/deflate (cg/soak :e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5)))))
  (is (= "(e4 c5 \"the sicilian defence\")"
        (pr-str (cd/deflate (cg/soak {:piece :P :to 28} {:piece :P :to 34} "the sicilian defence")))))
  (is (= "(e4 c5 (d5 \"the scandinavian defence\"))"
        (pr-str (cd/deflate (cg/soak {:piece :P :to 28} {:piece :P :to 34} :back {:piece :P :to 35} "the scandinavian defence")))))
  (is (= "(e4 $2 c5 (d5 $1 $13))"
        (pr-str (cd/deflate (cg/soak {:piece :P :to 28} :$2 {:piece :P :to 34} :back {:piece :P :to 35} :$1 :$13))))))

; read-string
; io/resource not available in cljs
; todo: fix
#?(:clj
   (deftest test-load-game
     (is (= "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
           (-> (cd/inflate-game (read-string "(e4 e5 (c5 Nc3 (g3 g6 Bg2 (a3 Bg7 (h5)))) Nf3)")) cf/fen)))
     (is (= "the sicilian defence"
           (-> (cd/inflate-game (read-string "(e4 c5 \"the sicilian defence\")")) zip/node :comment)))
     (is (= {:positional-assessment :$36}
           (-> (cd/inflate-game (read-string "(e4 c5 $36)")) zip/node :annotations)))
     (is (= {:move-assessment :$1, :positional-assessment :$13}
           (-> (cd/inflate-game (read-string "(e4 c5 $1 $13)")) zip/node :annotations)))
     (is (nil? (-> (cd/inflate-game (read-string "(e4 c5 $0)")) zip/node :annotations)))
     (is (= "8/Q6p/6p1/5p2/5P2/2p3P1/3r3P/2K1k3 b - - 1 44"
           (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp read-string cd/inflate-game cf/fen)))
     )
   )

;(time
;  (-> "games/deflated/complete-with-annotations.dgn" io/resource slurp read-string cd/load-game cf/fen))
;260ms