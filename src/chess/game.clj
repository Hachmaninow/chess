(ns chess.game
  (:require [chess.board :refer :all]
            [chess.pgn :refer :all]
            [chess.fen :refer :all]
            [criterium.core :refer :all]
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

(defn king-covered?
  "Check if the given move applied to the given game covers the king from opponent's checks."
  [game move]
  (let [opponent-moves (find-moves (update-board (:board game) move) (opponent (:turn game)))]
    (not-any? #(or (= (% :capture) :K) (= (% :capture) :k)) opponent-moves))) ; Here the color of the king does not matter, as only the right one will occur anyways.

(defn valid-moves
  "Find all valid moves in the given game considering check situations."
  [game]
  (filter #(king-covered? game %) (find-moves (:board game) (:turn game))))

(defn has-moves? [game]
  (some #(king-covered? game %) (find-moves (:board game) (:turn game))))

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
        has-moves (has-moves? new-game)
        gives-check (gives-check? new-board (:turn game))]
    (into new-game {:call (call has-moves gives-check)})))

;(let [start (System/currentTimeMillis)]
;  (play (new-game) "1.c4 d5 2.Qb3 Bh3 3.gxh3 f5 4.Qxb7 Kf7 5.Qxa7 Kg6 6.f3 c5 7.Qxe7 Rxa2 8.Kf2 Rxb2 9.Qxg7+ Kh5 10.Qxg8 Rxb1 11.Rxb1 Kh4 12.Qxh8 h5 13.Qh6 Bxh6 14.Rxb8 Be3+ 15.dxe3 Qxb8 16.Kg2 Qf4 17.exf4 d4 18.Be3 dxe3")
;  (println (- (System/currentTimeMillis) start))
;  )

(defn select-move [game parsed-move]
  (let [valid-moves (valid-moves game)
        matching-moves (filter #(matches-parsed-move? parsed-move %) valid-moves)]
    (condp = (count matching-moves)
      1 (first matching-moves)
      0 (throw (new IllegalArgumentException (str "No matching moves for: " parsed-move " within valid moves: " (seq valid-moves))))
      (throw (new IllegalArgumentException (str "Multiple matching moves: " (seq matching-moves) " for: " parsed-move " within valid moves: " (seq valid-moves)))))
    ))

(defn play-first-move [game parsed-moves]
  (if (empty? parsed-moves)
    game
    (recur (play-move game (select-move game (first parsed-moves))) (rest parsed-moves))))

(defn play
  "For the given game, play the moves contained in the given move-text and return an updated game."
  [game move-text]
  (play-first-move game (parse-move-text move-text)))

; (criterium.core/bench (valid-moves (new-game)))