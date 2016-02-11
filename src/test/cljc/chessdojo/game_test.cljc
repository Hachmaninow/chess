(ns chessdojo.game-test
  #?(:clj
     (:require [clojure.test :refer :all]
               [clojure.zip :as zip :refer [vector-zip up down left lefts right rights rightmost insert-right branch? node path root]]
               [chessdojo.game :as cg :refer [navigate]]
               [chessdojo.rules :as cr :refer [to-idx to-sqr]]
               [chessdojo.fen :as cf]
               ))
  #?(:cljs (:require [cljs.test :refer-macros [deftest is testing run-tests]]
             [clojure.zip :as zip :refer [vector-zip up down left lefts right rights rightmost insert-right branch? node path root]]
             [chessdojo.game :as cg :refer [navigate]]
             [chessdojo.rules :as cr :refer [to-idx to-sqr]]
             [chessdojo.fen :as cf])))

;
; verify zipper basics
;

(deftest test-zipper-learning
  (testing "vector-zipper"
    (let [zipper (vector-zip [1 2 3 4 [:a :b] [:c :d] 5])]
      (is (= [1 2 3 4 [:a :b] [:c :d] 5] (-> zipper root)))
      (is (= 1 (-> zipper down node)))
      (is (= 2 (-> zipper down right node)))
      (is (= 2 (-> zipper down right node)))
      (is (= :a (-> zipper down right right right right down node)))
      (testing "down"
        (is (= :a (-> zipper down right right right right down node)))
        (is (= [:a :b] (-> zipper down right right right right down up node))))))
  (testing "zip/node"
    (is (= {
            :move {:ep-info [20 28] :from 12 :piece :P :to 28}
            :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P nil :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r] :castling-availability {:black #{:O-O :O-O-O} :white #{:O-O :O-O-O}} :ep-info [20 28] :turn :black :ply 2}
            }
           (node (cg/insert-move cg/new-game (cr/parse-simple-move :e4))))))
  (testing "zip/root"
    (is (= [
            {
             :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P :P :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r], :turn :white, :castling-availability {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}} :ply 1}
             }
            {
             :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P nil :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r], :turn :black, :castling-availability {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}}, :ep-info [20 28] :ply 2},
             :move {:piece :P, :from 12, :to 28, :ep-info [20 28]}
             }
            ] (root (cg/insert-move cg/new-game (cr/parse-simple-move :e4))))))
  (testing "zip/next"
    (is (= [:a :b] (-> (vector-zip [1 2 [:a :b] [:c :d] 3]) down zip/next zip/next node)))
    (is (= :a (-> (vector-zip [1 2 [:a :b] [:c :d] 3]) down zip/next zip/next zip/next node)))))

(deftest test-new-game
  (is (= cr/start-position (:position (zip/node cg/new-game))))
  )

(deftest test-move->long-str
  (is (= "O-O" (cg/move->long-str {:piece :K :castling :O-O})))
  (is (= "O-O-O" (cg/move->long-str {:piece :k :castling :O-O-O})))
  (is (= "a2-a3" (cg/move->long-str {:piece :P :from (to-idx :a2) :to (to-idx :a3)})))
  (is (= "e2-e1=N" (cg/move->long-str {:piece :p :from (to-idx :e2) :to (to-idx :e1) :promote-to :n})))
  (is (= "d6xe6ep" (cg/move->long-str {:piece :P :from (to-idx :d6) :to (to-idx :e6) :ep-capture (to-idx :e5)})))
  (is (= "Nf7xh8" (cg/move->long-str {:piece :N :from (to-idx :f7) :to (to-idx :h8) :capture :r}))))

(deftest test-ply->move-number
  (is (= "2..." (cg/ply->move-number 5)))
  (is (= "3." (cg/ply->move-number 6))))

; can be tested using simple vector-zippers
(deftest test-find-insert-loc
  (let [zipper (vector-zip [1 2 3])]
    (is (= 2 (-> zipper down right node)))
    (is (= 3 (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 3 4 [:a :b] [:c :d] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= 3 (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:c :d] (-> zipper down right right cg/find-anchor node)))
    (is (= 5 (-> zipper down right rightmost node)))
    (is (= 5 (-> zipper down right rightmost cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 [:a :b] [:c :d]])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] 4 [:c :d] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:a :b] (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:c :d] (-> zipper down right right cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] [:c :d] 4 [:e :f [:g :h [:i [:j]]]] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:e :f [:g :h [:i [:j]]]] (-> zipper down right right cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] [:c :d] 4 [:e :f] [:g :h] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:g :h] (-> zipper down right right cg/find-anchor node))))
  )

(deftest test-end-of-variation?
  (let [zipper (vector-zip [1 2 3 4 [:a :b] [:c :d]])]
    (is (= 3 (-> zipper down right right node)))
    (is (false? (-> zipper down cg/end-of-variation?)))
    (is (= 4 (-> zipper down right right right node)))
    (is (true? (-> zipper down right right right cg/end-of-variation?))))
  (let [zipper (vector-zip [1])]
    (is (= 1 (-> zipper down node)))
    (is (true? (-> zipper down cg/end-of-variation?)))))

(defn soak-str [& moves]
  (cg/game->str (cg/soak (map cr/parse-simple-move moves))))

(deftest test-soak1
  (is (= ">e2-e4" (soak-str :e4)))
  (is (= "e2-e4 >c7-c5" (soak-str :e4 :c5)))
  (is (= "e2-e4 c7-c5 >Ng1-f3" (soak-str :e4 :c5 :Nf3)))
  (is (= "e2-e4 c7-c5 Ng1-f3 (>Nb1-c3)" (soak-str :e4 :c5 :Nf3 :back :Nc3)))
  (is (= "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 >Nb8-c6)" (soak-str :e4 :c5 :Nf3 :back :Nc3 :Nc6)))
  (is (= "e2-e4 >c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6)" (soak-str :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out)))
  (is (= "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6) (>c2-c3)" (soak-str :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3)))
  (is (= "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6) (c2-c3 >Ng8-f6)" (soak-str :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6)))
  (is (= "e2-e4 c7-c5 >Ng1-f3 (Nb1-c3 Nb8-c6) (c2-c3 Ng8-f6)" (soak-str :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward)))
  (is (= "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6) (c2-c3 Ng8-f6) Nb8-c6 >d2-d4" (soak-str :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6 :d4)))

  (testing "subsequent moves with variations"
    (is (= "e2-e4 c7-c5 (e7-e5) Ng1-f3 (>Nb1-c3)" (soak-str :e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3)))
    (is (= "e2-e4 >c7-c5 (e7-e5) Ng1-f3 (Nb1-c3)" (soak-str :e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3 :out)))
    (is (= "e2-e4 c7-c5 (e7-e5) Ng1-f3 (Nb1-c3) (c2-c3) >Nb8-c6" (soak-str :e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3 :out :c3 :out :forward :Nc6))))

  (testing "nested variations"
    (is (= "d2-d4 d7-d5 (Ng8-f6 c2-c4 (c2-c3 g7-g6 g2-g3 (>a2-a3))) Ng1-f3" (soak-str :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :back :a3)))
    (is (= "e2-e4 e7-e5 (c7-c5 Nb1-c3 (g2-g3 g7-g6 Bf1-g2 (a2-a3 Bf8-g7 (>h7-h5)))) Ng1-f3" (soak-str :e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5)))
    (is (= "d2-d4 d7-d5 (Ng8-f6 c2-c4 (c2-c3 g7-g6 g2-g3) (>h2-h4)) Ng1-f3" (soak-str :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :out :h4)))
    )

  (testing "nested variation in very first move"
    (is (= "d2-d4 (e2-e4 e7-e5 (>c7-c5)) d7-d5" (soak-str :d4 :d5 :back :back :e4 :e5 :back :c5)))))

(defn soak-fen [& moves]
  (cg/game->board-fen (cg/soak (map cr/parse-simple-move moves))))

(deftest test-soak2
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" (soak-fen)))
  (is (= "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR" (soak-fen :e4)))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR" (soak-fen :e4 :c5)))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R" (soak-fen :e4 :c5 :Nf3)))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR" (soak-fen :e4 :c5 :Nf3 :back)))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR" (soak-fen :e4 :c5 :Nf3 :back :Nc3)))
  (is (= "r1bqkbnr/pp1ppppp/2n5/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR" (soak-fen :e4 :c5 :Nf3 :back :Nc3 :Nc6)))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR" (soak-fen :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out)))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/2P5/PP1P1PPP/RNBQKBNR" (soak-fen :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3)))
  (is (= "rnbqkb1r/pp1ppppp/5n2/2p5/4P3/2P5/PP1P1PPP/RNBQKBNR" (soak-fen :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6)))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R" (soak-fen :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward)))
  (is (= "r1bqkbnr/pp1ppppp/2n5/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R" (soak-fen :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6)))
  (is (= "r1bqkbnr/pp1ppppp/2n5/2p5/3PP3/5N2/PPP2PPP/RNBQKB1R" (soak-fen :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6 :d4))))

(defn soak-path [& moves]
  (cg/game-path (cg/soak (map cr/parse-simple-move moves))))

(deftest test-game-paths
  (testing "basics"
    (is (= [0 0 nil] (soak-path)))
    (is (= [1 0 nil] (soak-path :e4)))
    (is (= [2 0 nil] (soak-path :e4 :c5)))
    (is (= [3 0 nil] (soak-path :e4 :c5 :Nf3)))
    (is (= [2 0 nil] (soak-path :e4 :c5 :Nf3 :back)))
    (is (= [3 1 [2 0 nil]] (soak-path :e4 :c5 :Nf3 :back :Nc3)))
    (is (= [4 1 [2 0 nil]] (soak-path :e4 :c5 :Nf3 :back :Nc3 :Nc6)))
    (is (= [2 0 nil] (soak-path :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out)))
    (is (= [3 2 [2 0 nil]] (soak-path :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3)))
    (is (= [4 2 [2 0 nil]] (soak-path :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6)))
    (is (= [2 0 nil] (soak-path :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out)))
    (is (= [3 0 nil] (soak-path :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward))))
  (testing "subsequent moves with variations"
    (is (= [3 1 [2 0 nil]] (soak-path :e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3))))
  (testing "multiple variations"
    (is (= [2 3 [1 0 nil]] (soak-path :e4 :c5 :back :e5 :out :e6 :out :c6)))) ; third variation
  (testing "nested variations"
    (is (= [5 1 [4 1 [2 1 [1 0 nil]]]] (soak-path :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :back :a3))) ; "d2-d4 d7-d5 (Ng8-f6 c2-c4 (c2-c3 g7-g6 g2-g3 (>a2-a3))) Ng1-f3"
    (is (= [3 2 [2 1 [1 0 nil]]] (soak-path :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :out :h4)))
    (is (= [6 1 [5 1 [4 1 [2 1 [1 0 nil]]]]] (soak-path :e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5))))
  (testing "nested variation in very first move"
    (is (= [2 1 [1 1 [0 0 nil]]] (soak-path :d4 :d5 :back :back :e4 :e5 :back :c5))))
  (testing "paths of variation node"
    (is (= [1 [4 1 [2 1 [1 0 nil]]]] (cg/game-path (zip/up (cg/soak (map cr/parse-simple-move [:d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :back :a3])))))) ; third variation of that parent
    (is (= [3 [1 0 nil]] (cg/game-path (zip/up (cg/soak (map cr/parse-simple-move [:e4 :c5 :back :e5 :out :e6 :out :c6])))))) ; third variation of that parent
    )

  )

(deftest test-soak-with-move-coords
  (is (= "e2-e4 e7-e5 Ng1-f3 (Nb1-c3) Nb8-c6 Bf1-b5 a7-a6 >Bb5xc6"
         (cg/game->str (cg/soak [{:piece :P :to 28} {:piece :P :to 36} {:piece :N :to 21}
                                 :back {:piece :N :to 18} :out :forward
                                 {:piece :N :to 42} {:piece :B :to 33} {:piece :P :to 40}
                                 {:piece :B :capture :X :to 42}])))))

;
; navigation
;

(deftest test-navigate
  (testing "simple zipper"
    (let [zipper (down (vector-zip [1 2 3]))]
      (is (= 1 (-> zipper node)))
      (is (= 2 (-> zipper (navigate :forward) node)))
      (is (= 3 (-> zipper (navigate :forward) (navigate :forward) node)))
      (testing "navigatation beyond last move is ignored"
        (is (= 3 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) node))))))
  (testing "nested zipper"
    (let [zipper (down (vector-zip [1 2 [:a :b] [:c [:c1 :c2 :c3 [:c3a]] :d] 3]))]
      (is (= 1 (-> zipper node)))
      (is (= 2 (-> zipper (navigate :forward) node)))
      (is (= 3 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) node)))
      (is (= 2 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) (navigate :back) node)))
      (is (= 1 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) (navigate :back) (navigate :back) (navigate :back) (navigate :back) node)))))
  (testing "start"
    (let [zipper (down (vector-zip [1 2 [:a :b] [:c [:c1 :c2 :c3 [:c3a]] :d] 3]))]
      (is (= 1 (-> zipper (navigate :start) node)))
      (is (= 2 (-> zipper (navigate :start) (navigate :forward) node))))))

(deftest test-jump
  (let [game (cg/soak (map cr/parse-simple-move [:e4 :e5 :Nf3 :back :Nc3 :out :g3 :g6 :Bg2 :Bg7 :back :Bh6 :start]))]
    (testing "jump to existing paths return game with this path"
      (is (= "e2-e4" (cg/move->long-str (:move (node (cg/jump game [1 0 nil]))))))
      (is (= "Ng1-f3" (cg/move->long-str (:move (node (cg/jump game [3 0 nil]))))))
      (is (= "Nb1-c3" (cg/move->long-str (:move (node (cg/jump game [3 1 [2 0 nil]]))))))
      (is (= "Bf8-g7" (cg/move->long-str (:move (node (cg/jump game [6 2 [2 0 nil]]))))))
      (is (= "Bf8-h6" (cg/move->long-str (:move (node (cg/jump game [6 1 [5 2 [2 0 nil]]]))))))
      (testing "non-existing path return original game"
        (is (= game (cg/jump game [6 3 [2 0 nil]])))))))

(deftest test-annotate
  (is (= "the sicilian defence" (:comment (zip/node (cg/soak [{:piece :P :to 28} {:piece :P :to 34} {:comment "the sicilian defence"}]))))))