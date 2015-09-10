(ns chess.game
  (:require [chess.board :refer :all]))

(defn guess-castling-rights [board]
  {:white (set (remove nil? [(when (and (= :K (lookup board :e1)) (= :R (lookup board :h1))) :0-0)
                             (when (and (= :K (lookup board :e1)) (= :R (lookup board :a1))) :0-0-0)]))
   :black (set (remove nil? [(when (and (= :k (lookup board :e8)) (= :r (lookup board :h8))) :0-0)
                             (when (and (= :k (lookup board :e8)) (= :r (lookup board :a8))) :0-0-0)]))})

(defn setup
  ([] (setup init-board))
  ([board] {:board board :player-to-move :white :castling-rights (guess-castling-rights board)})
  ([board options] (conj (setup board) options)))



(defn make-move [game move]
  {:board          (place-pieces (game :board) (move :piece-movements))
   :player-to-move (opponent (game :player-to-move))
   }
  )

(defn valid-moves [game]
  (find-moves game))


(defn switch-player [game] (assoc game :player-to-move (opponent (game :player-to-move))))
