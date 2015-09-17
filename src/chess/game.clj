(ns chess.game
  (:require [chess.board :refer :all] [chess.pgn :refer :all]))

(defn guess-castling-rights [board]
  {:white (set (remove nil? [(when (and (= :K (lookup board :e1)) (= :R (lookup board :h1))) :0-0)
                             (when (and (= :K (lookup board :e1)) (= :R (lookup board :a1))) :0-0-0)]))
   :black (set (remove nil? [(when (and (= :k (lookup board :e8)) (= :r (lookup board :h8))) :0-0)
                             (when (and (= :k (lookup board :e8)) (= :r (lookup board :a8))) :0-0-0)]))})

(defn setup
  ([] (setup init-board))
  ([board] {
              :board board 
              :turn :white 
              :move-no 1 
              :castling-rights (guess-castling-rights board)
              :valid-moves (find-moves board :white)
           })
  )

(defn switch-player [game] (assoc game :turn (opponent (game :turn))))

(defn move-to-piece-movements [board {:keys [:castling :from :to :rook-from :rook-to]}]
  (if (or (= castling :O-O) (= castling :O-O-O)) 
    [nil from (board from) to nil rook-from (board rook-from) rook-to]
    [nil from (board from) to]
  ))

(defn make-move [game move]
  (let [new-board (place-pieces (game :board) (move-to-piece-movements (game :board) move))
        new-turn (opponent (game :turn))]
    {
      :board new-board
      :turn new-turn
      :valid-moves (find-moves new-board new-turn)
    }
  ))

(defn find-move-by-str [game move-str]
  "Find the only valid move in the given game that matches the given move-string."
  (let [all-valid-moves (game :valid-moves)
        parsed-move (pgn move-str)]
    )
  )

