(ns chess.game
  (:require [chess.rules :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [clojure.zip :as zip :refer [up down left lefts right rights rightmost insert-right branch?]]
            [spyscope.core]
            [taoensso.timbre.profiling]))

(def new-game
  (zip/down (zip/vector-zip [{:position (setup-position)}])))


;
; move to string
;

(defn move->long-str [{:keys [:piece :from :to :capture :castling :ep-capture :promote-to :highlight]}]
  (cond
    (nil? piece) ""
    castling (name castling)
    :else (str
            (when highlight ">")
            (if (not= (piece-type piece) :P) (name (piece-type piece)))
            (name (to-sqr from))
            (if (or capture ep-capture) "x" "-")
            (name (to-sqr to)) (if ep-capture "ep")
            (if promote-to (str "=" (name (piece-type promote-to)))))
    )
  )

;
; variations
;

(defn start-of-variation? [game]
  (nil? (left game))
  )

(defn end-of-variation? [game]
  (every? vector? (rights game)))

(defn navigate [game target]
  (case target
    :back (if (start-of-variation? game) game (first (remove branch? (iterate left (left game)))))
    :forward (if (end-of-variation? game) game (first (remove branch? (iterate right (right game)))))
    :out (second (remove branch? (iterate left (up game)))) ; variations are inserted after following move
    false
    ))

(defn goto-insert-loc
  "For a given game find the loc at which a new continuation can be inserted."
  [game]
  (let [right-sibling (right game)
        same-depth-sibling (when (and right-sibling (not (end-of-variation? right-sibling))) (first (remove branch? (iterate right right-sibling))))
        first-variation (when same-depth-sibling (right same-depth-sibling))]
    (cond
      (nil? right-sibling) game                             ; absolute end of variation
      (nil? same-depth-sibling) (rightmost game)
      (and first-variation (branch? first-variation)) (last (take-while #(and % (branch? %)) (iterate right first-variation))) ; a variation following -> find last of all
      :default same-depth-sibling                           ; usually: insert variation at sibling of same depth
      )))


(defn insert-node [zipper node]
  (cond
    ;(branch? zipper) (-> zipper (zip/append-child node) down)
    (end-of-variation? zipper) (-> (goto-insert-loc zipper) (insert-right node) right) ; end of a variation -> continue
    (right zipper) (-> (goto-insert-loc zipper) (insert-right [node]) right down) ; there are already items following -> add as last variation
    ))

(defn highlight-move [highlight-move move]
  (if (identical? highlight-move move) (assoc move :highlight true) move))

(defn variation->str [variation-vec highlighter-fn]
  (clojure.string/join " "
                       (map #(cond
                              (vector? %) (str "(" (variation->str % highlighter-fn) ")")
                              (:move %) (move->long-str (highlighter-fn (:move %)))
                              ) variation-vec
                            ))
  )


(defn game->str [game]
  (let [highlighter-fn (partial highlight-move (:move (zip/node game)))]
    (variation->str (rest (zip/root game)) highlighter-fn))) ; skip the first element as its the anchor containing the start position


(defn create-move-node
  "Create a new zipper node representing a hash with move and position."
  [game move-coords]
  (let [position (:position (zip/node game)) move (select-move position move-coords)]
    {:move move :position (update-position position move)}))

(defn insert-move
  [game move]
  (let [move-coords (if (map? move) move (parse-move (name move)))] ; move param can be move-coords already or string/keyword repr. of move
    (insert-node game (create-move-node game move-coords))))

(defn soak [events]
  (reduce
    #(try
      (or
        (navigate %1 %2)
        (insert-move %1 %2))
      (catch Exception e (throw (IllegalArgumentException. (str e "Trying to play: " %2 " in game:" (game->str %1)) e))))
    new-game
    events))

(defn load-pgn [pgn]
  (soak (pgn->events pgn)))


(defn game-benchmark []
  (load-pgn "1.c4 d5 2.Qb3 Bh3 3.gxh3 f5 4.Qxb7 Kf7 5.Qxa7 Kg6 6.f3 c5 7.Qxe7 Rxa2 8.Kf2 Rxb2 9.Qxg7+ Kh5 10.Qxg8 Rxb1 11.Rxb1 Kh4 12.Qxh8 h5 13.Qh6 Bxh6 14.Rxb8 Be3+ 15.dxe3 Qxb8 16.Kg2 Qf4 17.exf4 d4 18.Be3 dxe3")
  )
; 1.3s

(defn game-benchmark2 []
  (load-pgn (slurp "test/test-pgns/complete.pgn")))
; 50.2s

(taoensso.timbre.profiling/profile :info :Arithmetic (dotimes [n 10] (game-benchmark)))
