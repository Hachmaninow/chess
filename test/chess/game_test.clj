(ns chess.game-test
  (:require [clojure.test :refer :all]
            [chess.rules :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [chess.game :refer :all]
            [clojure.zip :as zip :refer [vector-zip up down left lefts right rights rightmost insert-right branch? node root]]))

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
            :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P nil :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r] :call nil :castling-availability {:black #{:O-O :O-O-O} :white #{:O-O :O-O-O}} :ep-info [20 28] :turn :black}
            }
           (node (insert-move new-game :e4)))))
  (testing "zip/root"
    (is (= [
            {
             :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P :P :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r], :turn :white, :call nil, :castling-availability {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}}}
             }
            {
             :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P nil :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r], :turn :black, :call nil, :castling-availability {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}}, :ep-info [20 28]},
             :move {:piece :P, :from 12, :to 28, :ep-info [20 28]}
             }
            ] (root (insert-move new-game :e4))))))

(deftest test-new-game
  (is (= start-position (:position (zip/node new-game))))
  )

(deftest test-move->long-str
  (is (= "O-O" (move->long-str {:piece :K :castling :O-O})))
  (is (= "O-O-O" (move->long-str {:piece :k :castling :O-O-O})))
  (is (= "a2-a3" (move->long-str {:piece :P :from (to-idx :a2) :to (to-idx :a3)})))
  (is (= "e2-e1=N" (move->long-str {:piece :p :from (to-idx :e2) :to (to-idx :e1) :promote-to :n})))
  (is (= "d6xe6ep" (move->long-str {:piece :P :from (to-idx :d6) :to (to-idx :e6) :ep-capture (to-idx :e5)})))
  (is (= "Nf7xh8" (move->long-str {:piece :N :from (to-idx :f7) :to (to-idx :h8) :capture :r}))))

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
      (is (= 1 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) (navigate :back) (navigate :back) (navigate :back) (navigate :back) node)))
      )
    )
  )

; can be tested using simple vector-zippers
(deftest test-goto-insert-loc
  (let [zipper (vector-zip [1 2 3])]
    (is (= 2 (-> zipper down right node)))
    (is (= 3 (-> zipper down right goto-insert-loc node)))
    (is (= 3 (-> zipper down right right goto-insert-loc node))))
  (let [zipper (vector-zip [1 2 3 4 [:a :b] [:c :d] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= 3 (-> zipper down right goto-insert-loc node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:c :d] (-> zipper down right right goto-insert-loc node)))
    (is (= 5 (-> zipper down right rightmost node)))
    (is (= 5 (-> zipper down right rightmost goto-insert-loc node))))
  (let [zipper (vector-zip [1 2 [:a :b] [:c :d]])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right goto-insert-loc node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] 4 [:c :d] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:a :b] (-> zipper down right goto-insert-loc node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:c :d] (-> zipper down right right goto-insert-loc node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] [:c :d]  4 [:e :f [:g :h [:i [:j]]]]  5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right goto-insert-loc node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:e :f [:g :h [:i [:j]]]] (-> zipper down right right goto-insert-loc node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] [:c :d] 4 [:e :f] [:g :h] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right goto-insert-loc node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:g :h] (-> zipper down right right goto-insert-loc node))))
  )

(deftest test-end-of-variation?
  (let [zipper (vector-zip [1 2 3 4 [:a :b] [:c :d]])]
    (is (= 3 (-> zipper down right right node)))
    (is (false? (-> zipper down end-of-variation?)))
    (is (= 4 (-> zipper down right right right node)))
    (is (true? (-> zipper down right right right end-of-variation?))))
  (let [zipper (vector-zip [1])]
    (is (= 1 (-> zipper down node)))
    (is (true? (-> zipper down end-of-variation?)))
    )
  )

(deftest test-insert-node
  (are [nodes exp-root] (is (= exp-root (zip/root (reduce #(if (keyword? %2) (navigate %1 %2) (insert-node %1 %2)) (zip/down (zip/vector-zip [:start])) nodes))))
                        ["e4"] [:start "e4"]
                        ["e4" "c5" "Nf3"] [:start "e4" "c5" "Nf3"]
                        ["e4" "c5" "Nf3" :back "Nc3"] [:start "e4" "c5" "Nf3" ["Nc3"]]
                        ["e4" "c5" "Nf3" :back "Nc3" :out] [:start "e4" "c5" "Nf3" ["Nc3"]]
                        ["e4" "c5" "Nf3" :back "Nc3" :out "c3"] [:start "e4" "c5" "Nf3" ["Nc3"] ["c3"]]
                        ))

(deftest test-soak-with-game->str
  (are [moves game-str]
    (let [game (soak moves)]
      (is (= game-str (game->str game))))
    [:e4] ">e2-e4"
    [:e4 :c5] "e2-e4 >c7-c5"
    [:e4 :c5 :Nf3] "e2-e4 c7-c5 >Ng1-f3"
    [:e4 :c5 :Nf3 :back] "e2-e4 >c7-c5 Ng1-f3"
    [:e4 :c5 :Nf3 :back :Nc3] "e2-e4 c7-c5 Ng1-f3 (>Nb1-c3)"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6] "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 >Nb8-c6)"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out] "e2-e4 >c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6)"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3] "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6) (>c2-c3)"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6] "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6) (c2-c3 >Ng8-f6)"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward] "e2-e4 c7-c5 >Ng1-f3 (Nb1-c3 Nb8-c6) (c2-c3 Ng8-f6)"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6] "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6) (c2-c3 Ng8-f6) >Nb8-c6"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6 :d4] "e2-e4 c7-c5 Ng1-f3 (Nb1-c3 Nb8-c6) (c2-c3 Ng8-f6) Nb8-c6 >d2-d4"

    [:e4 :c5 :back :e5 :out :forward :Nf3] "e2-e4 c7-c5 (e7-e5) >Ng1-f3"
    [:e4 :c5 :back :e5 :out :forward :Nf3 :back] "e2-e4 >c7-c5 (e7-e5) Ng1-f3"
    [:e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3] "e2-e4 c7-c5 (e7-e5) Ng1-f3 (>Nb1-c3)"
    [:e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3 :out] "e2-e4 >c7-c5 (e7-e5) Ng1-f3 (Nb1-c3)"
    [:e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3 :out :c3 :out :forward :Nc6] "e2-e4 c7-c5 (e7-e5) Ng1-f3 (Nb1-c3) (c2-c3) >Nb8-c6"
    )
  )

(deftest test-soak-with-game->str-nested-variations
  (are [moves game-str]
    (let [game (soak moves)]
      (is (= game-str (game->str game))))
    [:d4 :d5 :back :back] "d2-d4 d7-d5"
    [:d4 :d5 :back :back :e4] "d2-d4 (>e2-e4) d7-d5"
    [:d4 :d5 :back :back :e4 :e5 :back :c5] "d2-d4 (e2-e4 e7-e5 (>c7-c5)) d7-d5"
    )
  )

(deftest test-soak-with-position
  (are [moves fen last-move-str]
    (let [game (soak moves)]
      (is (= fen (game->board-fen game)))
      (is (= last-move-str (move->long-str (:move (zip/node game)))))
      )
    [] "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" ""
    [:e4] "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR" "e2-e4"
    [:e4 :c5] "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR" "c7-c5"
    [:e4 :c5 :Nf3] "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R" "Ng1-f3"
    [:e4 :c5 :Nf3 :back] "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR" "c7-c5"
    [:e4 :c5 :Nf3 :back :Nc3] "rnbqkbnr/pp1ppppp/8/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR" "Nb1-c3"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6] "r1bqkbnr/pp1ppppp/2n5/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR" "Nb8-c6"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out] "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR" "c7-c5"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3] "rnbqkbnr/pp1ppppp/8/2p5/4P3/2P5/PP1P1PPP/RNBQKBNR" "c2-c3"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6] "rnbqkb1r/pp1ppppp/5n2/2p5/4P3/2P5/PP1P1PPP/RNBQKBNR" "Ng8-f6"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward] "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R" "Ng1-f3"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6] "r1bqkbnr/pp1ppppp/2n5/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R" "Nb8-c6"
    [:e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6 :d4] "r1bqkbnr/pp1ppppp/2n5/2p5/3PP3/5N2/PPP2PPP/RNBQKB1R" "d2-d4"
    ))

(deftest test-soak-with-move-coords
  (is (= "e2-e4 e7-e5 Ng1-f3 (Nb1-c3) Nb8-c6 Bf1-b5 a7-a6 >Bb5xc6"
         (game->str (soak [{:to-file "e" :to-rank "4"} {:to-file "e" :to-rank "5"} {:piece "N" :to-file "f" :to-rank "3"}
                           :back {:piece "N" :to-file "c" :to-rank "3"} :out :forward
                           {:piece "N" :to-file "c" :to-rank "6"} {:piece "B" :to-file "b" :to-rank "5"} {:to-file "a" :to-rank "6"}
                           {:capture "x" :piece "B" :to-file "c" :to-rank "6"}]))))
  )

(deftest test-load-pgn
  (are [pgn fen game-str]
    (let [game (load-pgn pgn)]
      (is (= fen (game->board-fen game)))
      (is (= game-str (game->str game))))
    "e4 e5 Nf3 Nc6 Bb5 a6 Bxc6" "r1bqkbnr/1ppp1ppp/p1B5/4p3/4P3/5N2/PPPP1PPP/RNBQK2R" "e2-e4 e7-e5 Ng1-f3 Nb8-c6 Bf1-b5 a7-a6 >Bb5xc6"
    "e4 e5 Nf3 (Nc3) Nc6 Bb5 a6 Bxc6" "r1bqkbnr/1ppp1ppp/p1B5/4p3/4P3/5N2/PPPP1PPP/RNBQK2R" "e2-e4 e7-e5 Ng1-f3 (Nb1-c3) Nb8-c6 Bf1-b5 a7-a6 >Bb5xc6"
    "d4 d5 (Nf6 c4 (g3)) Nf3" "rnbqkbnr/ppp1pppp/8/3p4/3P4/5N2/PPP1PPPP/RNBQKB1R" "d2-d4 d7-d5 (Ng8-f6 c2-c4 (g2-g3)) >Ng1-f3"
    )
  )

(deftest load-complex-pgn
  (is (= "8/Q6p/6p1/5p2/5P2/2p3P1/3r3P/2K1k3" (game->board-fen (load-pgn (slurp "test/test-pgns/complete.pgn"))))))





