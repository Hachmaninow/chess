(ns chess.game
  (:require [chess.board :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [spyscope.core]))

(defn guess-castling-rights [board]
  {:white (set (remove nil? [(when (and (= :K (lookup board :e1)) (= :R (lookup board :h1))) :0-0)
                             (when (and (= :K (lookup board :e1)) (= :R (lookup board :a1))) :0-0-0)]))
   :black (set (remove nil? [(when (and (= :k (lookup board :e8)) (= :r (lookup board :h8))) :0-0)
                             (when (and (= :k (lookup board :e8)) (= :r (lookup board :a8))) :0-0-0)]))})

(defn new-game
  ([] (new-game init-board))
  ([board] {
            :board           board
            :turn            :white
            :move-no         1
            :castling-rights (guess-castling-rights board)
            }
    ))

(defn switch-player [game] (assoc game :turn (opponent (game :turn))))

(defn move-to-piece-movements [board {:keys [:castling :from :to :rook-from :rook-to]}]
  (if (or (= castling :O-O) (= castling :O-O-O))
    [nil from (board from) to nil rook-from (board rook-from) rook-to]
    [nil from (board from) to]
    ))

(defn make-move [game move]
  {
   :board (place-pieces (game :board) (move-to-piece-movements (game :board) move))
   :turn  (opponent (game :turn))
   })

(defn illegal-due-to-check? [game move]
  "Check if the given move applied to the given game leaves the king in check which renders the move illegal."
  (let [game-after-move (make-move game move)
        opponent-moves (find-moves (game-after-move :board) (game-after-move :turn))]
    (some #(or (= (% :capture) :K) (= (% :capture) :k)) opponent-moves))) ; Here the color of the king does not matter, as only the right one will occur anyways.

(defn valid-moves [game]
  "Find all geometrically possible moves on the current board and remove move which are illegal."
  ( remove #(illegal-due-to-check? game %) (find-moves (game :board) (game :turn))))

(defn select-move [game parsed-move]
  (let [valid-moves (valid-moves game)
        matching-moves (filter #(matches-parsed-move? parsed-move %) valid-moves)]
    (condp = (count matching-moves)
      1 (first matching-moves)
      0 (throw (new IllegalArgumentException (str "No matching moves for: " parsed-move " within valid moves: " (seq valid-moves))))
      (throw (new IllegalArgumentException (str "Multiple matching moves: " (seq matching-moves) " for: " parsed-move " within valid moves: " (seq valid-moves)))))
    ))

(defn play-first-move [game parsed-moves]
  (if (empty? parsed-moves) game (recur (make-move game (select-move game (first parsed-moves))) (rest parsed-moves))))

(defn play [game move-text]
  "For the given game, play the moves contained in the given move-text and return an updated game."
  (play-first-move game (parse-move-text move-text)))

(to-fen-board ((play (new-game) "e4 e5 Nf3 Nf6") :board))
