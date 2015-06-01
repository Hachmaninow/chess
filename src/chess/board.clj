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

(defn direction-vector [index direction]
  (let [step-size (direction-steps direction) limit (if (pos? step-size) 64 -1)]
    (for [next (range (+ index step-size) limit step-size) :while (neighboring-squares? next (- next step-size))] next)))

(defprotocol Piece
  (valid-moves [origin-index position])
  )

(defrecord King [color] Piece
  
  )

(defrecord Queen [color] Piece
  )

(defrecord Rook [color] Piece
  )

(defrecord Bishop [color] Piece
  )

(defrecord Knight [color] Piece
  )

(defrecord Pawn [color] Piece
  )

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
  (assoc board (to-index square) piece)
  )

(def start-position
  (reduce place-piece empty-board
          (for [piece (keys all-pieces)
                square (get-in all-pieces [piece :initial-squares])] [piece square])))

(defn lookup [board square] (board (to-index square)))