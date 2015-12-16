(ns chess.game
  (:require [chess.rules :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [clojure.zip :as zip]
            [spyscope.core]
            [taoensso.timbre.profiling]))

(def new-game
  {
   :position (setup-position)
   :lines (zip/vector-zip [])
   }
  )


;
; move to string
;

(defn move->long-str [{:keys [:piece :from :to :capture :castling :ep-capture :promote-to]}]
  (cond
    (nil? piece) "error"
    castling (name castling)
    :else (str
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

(defn navigate [game target]
  (case target
    :up (:up game)
    :start (loop [prev (:up game)] (if prev (recur game) game))
    ))


(defn append-to-current-location [z move]
  (cond
    (zip/branch? z) (-> z (zip/append-child move) (zip/down))
    (nil? (zip/rights z)) (-> z (zip/insert-right move) (zip/right))
    ))


(defn- start-variation [game]
  (assoc game :lines (append-to-current-location (:lines game) [])))

(defn- end-variation [game]
  (assoc game :lines (zip/up (zip/up (:lines game)))))


;
;
;

(defn add-move
  "Add the given move to the current location in the given game."
  [{:keys [position lines]} move]
  {
    :position (update-position position move)
    :lines (append-to-current-location lines move)
    })


(defn add-line [game line])

(defn add-token [game token]
  (condp = (first token)
    :move (add-move game (select-move (:position game) (into {} (rest token)))) ; [:move [:to-file "d"] [:to-rank "4"]] -> {:to-file "d" :to-rank "4"}
    :variation (end-variation (add-line (start-variation game) (rest token)))
    game
    )
  )

(defn add-line [game line]
  (reduce add-token game line)
  )

(defn load-pgn [pgn-str]
  (add-line new-game (pgn pgn-str))
  )

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
