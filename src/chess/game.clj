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

(declare valid-moves)

(defn call [has-moves is-checked]
  (cond
    (and has-moves is-checked) :check
    (and (not has-moves) is-checked) :checkmate
    (and (not has-moves) (not is-checked)) :stalemate))

(defn play-move
  "Update the given game by playing the given move."
  [game move]
  (let [new-board (update-board (:board game) move)
        new-turn (opponent (:turn game))
        new-game {:board new-board :turn new-turn}
        has-moves (> (count (valid-moves new-game)) 0)
        gives-check (gives-check? new-board (:turn game))]
    (into new-game {:call (call has-moves gives-check)})))

(defn illegal-due-to-check?
  "Check if the given move applied to the given game leaves the king in check which renders the move illegal."
  [game move]
  (let [opponent-moves (find-moves (update-board (:board game) move) (opponent (:turn game)))]
    (some #(or (= (% :capture) :K) (= (% :capture) :k)) opponent-moves))) ; Here the color of the king does not matter, as only the right one will occur anyways.

(defn valid-moves
  "Find all valid moves in the given game considering check situations."
  [game]
  (remove #(illegal-due-to-check? game %) (find-moves (:board game) (:turn game))))

(defn select-move [game parsed-move]
  (let [valid-moves (valid-moves game)
        matching-moves (filter #(matches-parsed-move? parsed-move %) valid-moves)]
    (condp = (count matching-moves)
      1 (first matching-moves)
      0 (throw (new IllegalArgumentException (str "No matching moves for: " parsed-move " within valid moves: " (seq valid-moves))))
      (throw (new IllegalArgumentException (str "Multiple matching moves: " (seq matching-moves) " for: " parsed-move " within valid moves: " (seq valid-moves)))))
    ))

(defn play-first-move [game parsed-moves]
  (if (empty? parsed-moves) game (recur (play-move game (select-move game (first parsed-moves))) (rest parsed-moves))))

(defn play [game move-text]
  "For the given game, play the moves contained in the given move-text and return an updated game."
  (play-first-move game (parse-move-text move-text)))

(to-fen-board ((play (new-game) "e4 e5 Nf3 Nf6") :board))
