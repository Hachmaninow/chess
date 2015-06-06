(ns chess.board)

(defn rank [index] (int (/ index 8)))

(defn file [index] (int (rem index 8)))

(defn to-index [square]
  (let [square-name (name square) file (first square-name) rank (second square-name)]
    (+ (- (int file) (int \a)) (* 8 (- (int rank) (int \1)))))
  )

(defn to-square [index] (keyword (str (char (+ (file index) (int \a))) (inc (rank index)))))

(defn neighboring-squares? [i1 i2]
  (let [rd (- (rank i1) (rank i2)) fd (- (file i1) (file i2))]
    (and
      (or (= 1 rd) (= 0 rd) (= -1 rd))
      (or (= 1 fd) (= 0 fd) (= -1 fd))
      (not= 0 rd fd))))

(def direction-steps {:N 8 :S -8 :W -1 :E 1 :NE 9 :SE -7 :SW -9 :NW 7})

(defn direction-vector [index max-reach direction]
  (let [step-size (direction-steps direction) limit (if (pos? step-size) 64 -1)]
    (take max-reach (for [next (range (+ index step-size) limit step-size) :while (neighboring-squares? next (- next step-size))] next))))

(defn piece-color [piece] (if (nil? piece) nil (if (contains? #{:P :N :B :R :Q :K} piece) :white :black)))

(defn occupied-indexes [board color] (set (filter #(= color (piece-color (board %))) (range 0 64))))

(defn empty-square? [board index] (nil? (get board index)))

(defn reachable-indexes [from-index board max-reach directions]
  (remove
    (occupied-indexes board (piece-color (get board from-index)))
    (flatten
      (for [direction directions]
        (concat
          (take-while #(empty-square? board %) (direction-vector from-index max-reach direction))
          (take 1 (drop-while #(empty-square? board %) (direction-vector from-index max-reach direction))))))))

(defprotocol Piece (find-moves [self index board]))

(defrecord King [color] Piece
  (find-moves [_ from-index board]
    (reachable-indexes from-index board 1 [:N :E :S :W :NE :SE :SW :NW])))

(defrecord Queen [color] Piece
  (find-moves [_ from-index board]
    (reachable-indexes from-index board 7 [:N :E :S :W :NE :SE :SW :NW])))

(defrecord Rook [color] Piece
  (find-moves [_ from-index board]
    (reachable-indexes from-index board 7 [:N :E :S :W])))

(defrecord Bishop [color] Piece
  (find-moves [_ from-index board]
    (reachable-indexes from-index board 7 [:NE :SE :SW :NW])))

(defrecord Knight [color] Piece
  (find-moves [_ from-index board] nil))

(defrecord Pawn [color] Piece
  (find-moves [_ from-index board] nil))

(def all-pieces
  {:K {:type (->King [:white]) :initial-squares [:e1]} :k {:type (->King [:black]) :initial-squares [:e8]}
   :Q {:type (->Queen [:white]) :initial-squares [:d1]} :q {:type (->Queen [:black]) :initial-squares [:d8]}
   :R {:type (->Rook [:white]) :initial-squares [:a1 :h1]} :r {:type (->Rook [:black]) :initial-squares [:a8 :h8]}
   :N {:type (->Knight [:white]) :initial-squares [:b1 :g1]} :n {:type (->Knight [:black]) :initial-squares [:b8 :g8]}
   :B {:type (->Bishop [:white]) :initial-squares [:c1 :f1]} :b {:type (->Bishop [:black]) :initial-squares [:c8 :f8]}
   :P {:type (->Pawn [:white]) :initial-squares [:a2 :b2 :c2 :d2 :e2 :f2 :g2 :h2]} :p {:type (->Pawn [:black]) :initial-squares [:a7 :b7 :c7 :d7 :e7 :f7 :g7 :h7]}
   })

(def empty-board (vec (repeat 64 nil)))

(defn place-piece [board [piece square]]
  (assoc board (to-index square) piece))

(defn place-pieces [board piece-positions]
  (reduce place-piece board (for [[piece square] (partition 2 piece-positions)] [piece square])))

(def start-position
  (reduce place-piece empty-board
          (for [piece (keys all-pieces)
                square (get-in all-pieces [piece :initial-squares])] [piece square])))

(defn lookup [board square] (board (to-index square)))




;  (use 'clojure.set)