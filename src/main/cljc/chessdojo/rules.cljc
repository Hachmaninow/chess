(ns chessdojo.rules
  (:require [clojure.set :refer [intersection]]))

;
; basics
;

(defn opponent [color] (if (= color :white) :black :white))

(defn piece-color [piece] (if (nil? piece) nil (if (contains? #{:P :N :B :R :Q :K} piece) :white :black)))

(def piece-type {:K :K :Q :Q :R :R :B :B :N :N :P :P :k :K :q :Q :r :R :b :B :n :N :p :P :x :X})

(def colored-piece-lookup {:white {:K :K :Q :Q :R :R :B :B :N :N :P :P} :black {:K :k :Q :q :R :r :B :b :N :n :P :p}})

(defn colored-piece [turn piece-type]
  (get-in colored-piece-lookup [turn piece-type]))

(def square-names [:a1 :b1 :c1 :d1 :e1 :f1 :g1 :h1
                   :a2 :b2 :c2 :d2 :e2 :f2 :g2 :h2
                   :a3 :b3 :c3 :d3 :e3 :f3 :g3 :h3
                   :a4 :b4 :c4 :d4 :e4 :f4 :g4 :h4
                   :a5 :b5 :c5 :d5 :e5 :f5 :g5 :h5
                   :a6 :b6 :c6 :d6 :e6 :f6 :g6 :h6
                   :a7 :b7 :c7 :d7 :e7 :f7 :g7 :h7
                   :a8 :b8 :c8 :d8 :e8 :f8 :g8 :h8
                   ])

(def square-name-lookup
  (into {} (map #(vector (get square-names %) %) (range 0 64))))

(defn to-idx [square] (square square-name-lookup))

(defn to-sqr [index] (get square-names index))


;
; board arithmetics
;

(defn rank [idx] (int (/ idx 8)))

(defn file [index] (int (rem index 8)))

(defn distance [i1 i2]
  (max (Math/abs (- (rank i1) (rank i2))) (Math/abs (- (file i1) (file i2)))))

(defn indexes-between [i1 i2]
  (range (min i1 i2) (inc (max i1 i2))))

(defn still-on-board? [idx]
  (and (< idx 64) (>= idx 0)))


;
; lookups
;

(defn lookup [board square]
  (board (to-idx square)))

(defn find-piece [board piece]
  (let [pos (first (filter #(= piece (board %)) (range 0 64)))]
    (if (nil? pos) -1 pos)))                                ; TODO: Clean this up!  was previously . indexOf

(defn is-piece? [board idx turn piece-type]
  (= (board idx) (colored-piece turn piece-type)))

(defn occupied-indexes
  "Return all indexes on the given board occupied by given player or given player/piece-type combination."
  ([board turn] (occupied-indexes board turn nil))
  ([board turn {from :from piece-type :piece}]
   (filter #(if piece-type (is-piece? board % turn piece-type) (= (piece-color (board %)) turn))
           (if from (vector from) (range 0 64)))))

(defn empty-square? [board index]
  (nil? (get board index)))


;
; board setup
;

(def empty-board (vec (repeat 64 nil)))

(defn place-piece [board [piece square-or-index]]
  (assoc board (if (keyword? square-or-index) (to-idx square-or-index) square-or-index) piece))

(defn place-pieces
  "Place the given array of piece-locations on an empty/given board. Piece positions are adjacent elements of piece/square-or-index pairs."
  ([piece-locations] (place-pieces empty-board piece-locations))
  ([board piece-locations] (reduce place-piece board (for [[piece square-or-index] (partition 2 piece-locations)] [piece square-or-index]))))


;
; update board
;

(defn move-to-piece-movements [board {:keys [:castling :from :to :rook-from :rook-to :ep-capture :promote-to]}]
  (cond
    castling [nil from (board from) to nil rook-from (board rook-from) rook-to]
    ep-capture [nil from nil ep-capture (board from) to]
    promote-to [nil from promote-to to]
    :else [nil from (board from) to]))

(defn update-board [board move]
  (place-pieces board (move-to-piece-movements board move)))


;
; basic piece movement
;

(def direction-steps {:N 8 :S -8 :W -1 :E 1 :NE 9 :SE -7 :SW -9 :NW 7})
(def straight [:N :E :S :W])
(def diagonal [:NE :SE :SW :NW])
(def straight-and-diagonal (concat straight diagonal))
(def knight-steps [+17 +10 -6 -15 -17 -10 +6 +15])


(defn direction-vector-internal [index max-reach direction]
  (let [step-size (direction-steps direction) limit (if (pos? step-size) 64 -1)]
    (take max-reach
          (for [next (range (+ index step-size) limit step-size) :while (= 1 (distance next (- next step-size)))] next))))

(def direction-vector (memoize direction-vector-internal))

(defn reachable-indexes [from-index board max-reach directions]
  (flatten
    (for [direction directions]
      (let [direction-vector (direction-vector from-index max-reach direction)]
        (concat
          (take-while #(empty-square? board %) direction-vector)
          (take 1 (drop-while #(empty-square? board %) direction-vector))))))) ; TODO: This seems quite inefficient.

;
; attacks
;

(defmulti attacked-indexes (fn [board _ idx] (piece-type (get board idx))))

(defmethod attacked-indexes :K [board _ idx] (reachable-indexes idx board 1 straight-and-diagonal))

(defmethod attacked-indexes :Q [board _ idx] (reachable-indexes idx board 7 straight-and-diagonal))

(defmethod attacked-indexes :R [board _ idx] (reachable-indexes idx board 7 straight))

(defmethod attacked-indexes :B [board _ idx] (reachable-indexes idx board 7 diagonal))

(defmethod attacked-indexes :N [_ _ idx]
  (filter #(and (= (distance % idx) 2) (still-on-board? %)) (map #(+ idx %) knight-steps)))

(defmethod attacked-indexes :P [_ turn idx]
  (let [op (if (= :white turn) + -) s1 (op idx 7) s2 (op idx 9)]
    (vector
      (when (= (distance idx s1) 1) s1)
      (when (= (distance idx s2) 1) s2))))

(defn find-attacks
  "Return all attacking moves from a given index of player on a board with given occupied indexes."
  [board turn idx]
  (let [piece (get board idx) target-indexes (attacked-indexes board turn idx)]
    (map #(when % {:piece piece :from idx :to % :capture (board %)}) target-indexes)))


;
; pawn moves
;

(def promotions {:white [:Q :R :B :N] :black [:q :r :b :n]})

(defn on-rank? [target-rank turn idx]
  (if (= turn :white)
    (= (rank idx) target-rank)
    (= (rank idx) (- 7 target-rank))))

(defn handle-promotions [turn move]
  (if (on-rank? 6 turn (:from move)) (map #(into move {:promote-to %}) (turn promotions)) move))

(defn find-simple-pawn-moves [board turn idx]
  (let [piece (get board idx) op (if (= turn :white) + -) s1 (op idx 8)]
    (when (empty-square? board s1)
      (->> {:piece piece :from idx :to s1}
           (handle-promotions turn)
           vector
           flatten))))

(defn find-double-pawn-moves [board turn idx]
  (let [piece (get board idx) op (if (= turn :white) + -) s1 (op idx 8) s2 (op idx 16)]
    (when (and (on-rank? 1 turn idx) (empty-square? board s1) (empty-square? board s2))
      (vector {:piece piece :from idx :to s2 :ep-info [s1 s2]}))))

(defn find-capturing-pawn-moves [board turn idx]
  (->> (find-attacks board turn idx)
       (filter #(= (piece-color (:capture %)) (opponent turn)))
       (map (partial handle-promotions turn))
       flatten))

(defn find-en-passant-moves [board turn [ep-target ep-capture] idx]
  (when ep-target
    (->> (find-attacks board turn idx)
         (filter #(= ep-target (:to %)))
         (map #(assoc % :ep-capture ep-capture)))))

(defn find-pawn-moves [board turn en-passant-info idx]
  (concat
    (find-simple-pawn-moves board turn idx)
    (find-double-pawn-moves board turn idx)
    (find-capturing-pawn-moves board turn idx)
    (find-en-passant-moves board turn en-passant-info idx)))


;
; under-attack?
;

(defn first-piece-in-direction [board idx max-reach direction]
  (first (filter (complement nil?) (map board (direction-vector idx max-reach direction)))))

(defn attacked-by-piece-types? [board idx max-reach directions piece-types]
  (some #(piece-types (first-piece-in-direction board idx max-reach %)) directions))

(defn attacked-by-knight? [board idx turn]
  (first
    (filter
      #(and (= (distance % idx) 2) (still-on-board? %) (is-piece? board % turn :N)) (map #(+ idx %) knight-steps))))

(defn attacked-by-pawn? [board idx turn]
  (let [op (if (= :white turn) - +) s1 (op idx 7) s2 (op idx 9)]
    (or
      (and (= (distance idx s1) 1) (still-on-board? s1) (is-piece? board s1 turn :P))
      (and (= (distance idx s2) 1) (still-on-board? s2) (is-piece? board s2 turn :P)))))

(defn under-attack? [board idx turn]
  (or
    (attacked-by-piece-types? board idx 7 straight #{(colored-piece turn :Q) (colored-piece turn :R)})
    (attacked-by-piece-types? board idx 7 diagonal #{(colored-piece turn :Q) (colored-piece turn :B)})
    (attacked-by-piece-types? board idx 1 straight-and-diagonal #{(colored-piece turn :K)})
    (attacked-by-knight? board idx turn)
    (attacked-by-pawn? board idx turn)
    ))


;
; castlings
;

(def castlings
  {:white {
           :O-O   {:piece :K :from (to-idx :e1) :to (to-idx :g1) :rook-from (to-idx :h1) :rook-to (to-idx :f1)}
           :O-O-O {:piece :K :from (to-idx :e1) :to (to-idx :c1) :rook-from (to-idx :a1) :rook-to (to-idx :d1)}}
   :black {
           :O-O   {:piece :k :from (to-idx :e8) :to (to-idx :g8) :rook-from (to-idx :h8) :rook-to (to-idx :f8)}
           :O-O-O {:piece :k :from (to-idx :e8) :to (to-idx :c8) :rook-from (to-idx :a8) :rook-to (to-idx :d8)}}})

(defn- check-castling [board turn [castling-type {:keys [from to rook-from rook-to] :as rules}]]
  (let [kings-route (set (indexes-between from to))         ; all the squares the king passes and which must not be under attack
        passage (filter #(not= rook-from %) (indexes-between rook-from rook-to))] ; all squares between the king and the rook, which must be unoccupied
    (when
      (and
        (is-piece? board from turn :K)
        (is-piece? board rook-from turn :R)
        (every? (partial empty-square? board) passage)
        (not-any? #(under-attack? board % (opponent turn)) kings-route))
      (assoc rules :castling castling-type))))

(defn find-castlings [board turn]
  (remove nil? (map (partial check-castling board turn) (castlings turn))))

(defn deduce-castling-availability [board]
  {:white (set (remove nil? [(when (and (= :K (lookup board :e1)) (= :R (lookup board :h1))) :O-O)
                             (when (and (= :K (lookup board :e1)) (= :R (lookup board :a1))) :O-O-O)]))
   :black (set (remove nil? [(when (and (= :k (lookup board :e8)) (= :r (lookup board :h8))) :O-O)
                             (when (and (= :k (lookup board :e8)) (= :r (lookup board :a8))) :O-O-O)]))})

(defn intersect-castling-availability [castling-availability new-castling-availability]
  {
   :white (intersection (:white castling-availability) (:white new-castling-availability))
   :black (intersection (:black castling-availability) (:black new-castling-availability))
   })

(defn castling-available?
  "Check if in the given game with specific castling-availability a given castling is valid."
  [{:keys [castling-availability turn]} {:keys [castling]}]
  (castling (turn castling-availability)))

;
; find valid moves
;

(defn criteria-matcher [{:keys [:castling :piece :from :to :capture :from-file :from-rank :promote-to]}]
  (remove nil?
          (vector
            (when castling (fn [move] (= (:castling move) castling)))
            (when from (fn [move] (= (:from move) from)))
            (when to (fn [move] (= (:to move) to)))
            (when piece (fn [move] (= (piece-type (:piece move)) piece))) ; if a piece is specified, it does not matter if it's black or white
            (when capture (fn [move] (or (move :capture) (move :ep-capture))))
            (when from-file (fn [move] (= (file (:from move)) from-file)))
            (when from-rank (fn [move] (= (rank (:from move)) from-rank)))
            ;(when promote-to (fn [move] (= (keyword promote-to) (piece-type (move :promote-to)))))
            )))

(defn matches-criteria? [valid-move criteria]
  (every? #(% valid-move) (criteria-matcher criteria)))

(defn find-moves
  "Find all possible moves on the given board and player without considering check situation, but considering given criteria."
  ([board turn] (find-moves board turn nil nil))
  ([board turn en-passant-info criteria]
   (let [owned-indexes (occupied-indexes board turn criteria)]
     (remove #(= turn (piece-color (% :capture)))           ; remove all moves to squares already owned by the player
             (remove nil?
                     (mapcat #(if (is-piece? board % turn :P) (find-pawn-moves board turn en-passant-info %) (find-attacks board turn %))
                             owned-indexes))))))

(defn king-covered?
  "Check if the given move applied to the given board covers the king from opponent's checks."
  [board turn move]
  (let [new-board (update-board board move)
        kings-pos (find-piece new-board (colored-piece turn :K))]
    (not (under-attack? new-board kings-pos (opponent turn)))))

(defn valid-moves
  "Find all valid moves in the given game considering check situations and considering given criteria."
  ([position] (valid-moves position nil))
  ([{:keys [board turn ep-info] :as position} criteria]
   (concat
     (filter #(king-covered? board turn %)
             (filter #(matches-criteria? % criteria)        ; filter moves using criteria to save expensive check for king-coverage
                     (find-moves board turn ep-info criteria))) ; use criteria even for restricting move candidate generation
     (filter #(castling-available? position %)
             (find-castlings board turn)))))


;
; position
;

(defn gives-check?
  "Check if the given player gives check to the opponent's king on the current board."
  [board turn]
  (some #{:K :k} (map :capture (find-moves board turn))))   ; Here the color of the king does not matter, as only the right one will occur anyways.

(defn has-moves?
  [board turn]
  (some #(king-covered? board turn %) (find-moves board turn)))

(defn call
  "Check if the last move by the opponent of the given player included a check, checkmate or stalemate call."
  [{:keys [board turn]}]
  (let [gives-check (gives-check? board (opponent turn)) has-moves (has-moves? board turn)]
    (cond
      (and gives-check has-moves) :check
      (and gives-check (not has-moves)) :checkmate
      (and (not gives-check) (not has-moves)) :stalemate)))

(defn setup-position
  ([]
   (setup-position [:K :e1 :k :e8 :Q :d1 :q :d8 :R :a1 :R :h1 :r :a8 :r :h8
                    :N :b1 :N :g1 :n :b8 :n :g8 :B :c1 :B :f1 :b :c8 :b :f8
                    :P :a2 :P :b2 :P :c2 :P :d2 :P :e2 :P :f2 :P :g2 :P :h2
                    :p :a7 :p :b7 :p :c7 :p :d7 :p :e7 :p :f7 :p :g7 :p :h7]))
  ([piece-locations options]
   (merge (setup-position piece-locations) options))
  ([piece-locations]
   (let [board (place-pieces piece-locations)]
     {
      :board                 board
      :turn                  :white
      :castling-availability (deduce-castling-availability board)
      :ply                   1                              ; half-move clock awaited move
      })))

(def start-position (setup-position))


(defn update-position
  "Update the given position by playing the given move."
  [{:keys [board turn castling-availability ply]} move]
  (let [new-board (update-board board move)]
    {
     :board                 new-board
     :turn                  (opponent turn)
     :castling-availability (intersect-castling-availability castling-availability (deduce-castling-availability new-board))
     :ep-info               (:ep-info move)
     :ply                   (inc ply)
     }))


;
; move selection
;

(def rank-names {"1" 0 "2" 1 "3" 2 "4" 3 "5" 4 "6" 5 "7" 6 "8" 7}) ; TODO: investigate (int \a) not supported in cljs (???)
(def file-names {"a" 0 "b" 1 "c" 2 "d" 3 "e" 4 "f" 5 "g" 6 "h" 7}) ; TODO: investigate (int \1) not supported in cljs (???)
(defn- ex-piece [str] (keyword (subs str 0 1)))
(defn- ex-sqr [str index] (to-idx (keyword (subs str index))))
(defn- ex-file [str index] (get file-names (subs str index (inc index))))
(defn- ex-rank [str index] (get rank-names (subs str index (inc index))))

(defn parse-simple-move [input]
  "Regexp-based parsing of single moves for testing purposes (as Instaparse is unavailable in cljs tests)."
  (let [s (name input)]
    (cond
      (re-matches #"back|forward|out|start" s) input
      (re-matches #"O-O" s) {:piece :K :castling :O-O}
      (re-matches #"O-O-O" s) {:piece :K :castling :O-O-O}
      (re-matches #"[a-h][1-8]" s) {:piece :P :to (ex-sqr s 0)}
      (re-matches #"[N|B|R|Q|K].." s) {:piece (ex-piece s) :to (ex-sqr s 1)}
      (re-matches #"[a-h]x[a-h][1-8]" s) {:piece :P :from-file (ex-file s 0) :capture :X :to (ex-sqr s 2)}
      (re-matches #"[N|B|R|Q|K]x[a-h][1-8]" s) {:piece (ex-piece s) :capture :X :to (ex-sqr s 2)}
      (re-matches #"[N|B|R|Q|K][a-h][a-h][1-8]" s) {:piece (ex-piece s) :from-file (ex-file s 1) :to (ex-sqr s 2)}
      (re-matches #"[N|B|R|Q|K][1-8][a-h][1-8]" s) {:piece (ex-piece s) :from-rank (ex-rank s 1) :to (ex-sqr s 2)}
      (re-matches #"[N|B|R|Q|K][a-h]x[a-h][1-8]" s) {:piece (ex-piece s) :from-file (ex-file s 1) :capture :X :to (ex-sqr s 3)}
      (re-matches #"[N|B|R|Q|K][1-8]x[a-h][1-8]" s) {:piece (ex-piece s) :from-rank (ex-rank s 1) :capture :X :to (ex-sqr s 3)}
      )))



(defn select-move [position criteria]
  (let [valid-moves (valid-moves position criteria)
        matching-moves (filter #(matches-criteria? % criteria) valid-moves)]
    (condp = (count matching-moves)
      1 (first matching-moves)
      0 (throw (ex-info "No matching moves" {:for criteria :valid-moves (seq valid-moves)}))
      (throw (ex-info "Multiple matching moves" {:for criteria :valid-moves (seq valid-moves) :matching-moves matching-moves})))))