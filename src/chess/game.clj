(ns chess.game
  (:require [chess.rules :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [clojure.zip :as zip :refer [up down left lefts right rights insert-right branch?]]
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

(defn end-of-variation? [game]
  (every? vector? (rights game)))

(defn navigate [game target]
  (case target
    :back (left game)
    :forward (if (end-of-variation? game) game (first (remove branch? (iterate right (right game)))))
    :out (second (drop-while branch? (iterate left (up game))))
    false
    ))

(defn find-end-of-variation
  "For a given zipper find the loc at which a new sibling has to be appended."
  [zipper]
  (let [same-line-sibling (right zipper) first-variation (right same-line-sibling)]
    (cond
      (nil? same-line-sibling) zipper   ; absolute end of variation
      (and first-variation (branch? first-variation)) (last (take-while #(and % (branch? %)) (iterate right first-variation))) ; a variation following -> find last of all
      :default same-line-sibling     ; usually: insert variation as sibling of following move
    )))


(defn insert-node [zipper node]
  (cond
    ;(branch? zipper) (-> zipper (zip/append-child node) down)
    (end-of-variation? zipper) (-> (find-end-of-variation zipper) (insert-right node) right) ; end of a variation -> continue
    (right zipper) (-> (find-end-of-variation zipper) (insert-right [node]) right down) ; there are already items following -> add as last variation
))

(defn- start-variation [game]
  (insert-node game []))

(defn- end-variation [game]
  (zip/up game))

(defn highlight-move [highlight-move move]
  (if (identical? highlight-move move) (assoc move :highlight true) move))

(defn game->str [root highlighter-fn]
  (clojure.string/trim
    (let [h (first root)]
      (cond
        (nil? h) ""
        (vector? h) "()"
        (:move h) (str (move->long-str (highlighter-fn (:move h))) " " (game->str (rest root) highlighter-fn))
        :default (game->str (rest root) highlighter-fn)     ; skip
        )
      )))

(defn variation->str [variation-vec highlighter-fn]
  (clojure.string/join " "
                       (map #(cond
                              (vector? %) (str "(" (variation->str % highlighter-fn) ")")
                              (:move %) (move->long-str (highlighter-fn (:move %)))
                              ) variation-vec
                            ))
  )


(defn game->str [game highlighter-fn]
  (variation->str (rest (zip/root game)) highlighter-fn)    ; skip the first element as its the anchor containing the start position
  )

(defn create-move-node
  "Create a new zipper node."
  [game move-coords]
  (let [position (:position (zip/node game)) move (select-move position move-coords)]
    {:position (update-position position move) :move move}))

(defn insert-move
  [game move-coords]
  (insert-node game (create-move-node game move-coords))
  )


(defn- add-line [game line])   ; forward reference

(defn add-token [game token]
  (condp = (first token)
    :move (insert-move game (into {} (rest token)))         ; [:move [:to-file "d"] [:to-rank "4"]] -> {:to-file "d" :to-rank "4"}
    :variation (end-variation (add-line (start-variation game) (rest token)))
    game
    ))

(defn add-line [game line]
  (reduce add-token game line)
  )

(defn load-pgn [pgn-str]
  (add-line new-game (pgn pgn-str))
  )


(defn soak [items]
  (reduce #(if-let [game (navigate %1 %2)] game (insert-move %1 (parse-move (name %2)))) new-game items)
  )



;(load-pgn "d4 d5 Nf3")
;(load-pgn "d4 d5 Nf3 (Nc3)")


;(defn lines [game]
;  (let [game (start game)]
;    (loop [lines (:lines game) result []])
;
;    )
;  )

;(board->fen (:board (load-game (pgn (slurp "test/test-pgns/complete.pgn")))))

;(parse-move-text (slurp "test/test-pgns/complete.pgn"))

;(defn game-benchmark []
;  (play (new-game) "1.c4 d5 2.Qb3 Bh3 3.gxh3 f5 4.Qxb7 Kf7 5.Qxa7 Kg6 6.f3 c5 7.Qxe7 Rxa2 8.Kf2 Rxb2 9.Qxg7+ Kh5 10.Qxg8 Rxb1 11.Rxb1 Kh4 12.Qxh8 h5 13.Qh6 Bxh6 14.Rxb8 Be3+ 15.dxe3 Qxb8 16.Kg2 Qf4 17.exf4 d4 18.Be3 dxe3")
;  )
;
;(taoensso.timbre.profiling/profile :info :Arithmetic (dotimes [n 10] (game-benchmark)))
