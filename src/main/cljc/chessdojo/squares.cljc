(ns chessdojo.squares)

(def a1 0)
(def b1 1)
(def c1 2)
(def d1 3)
(def e1 4)
(def f1 5)
(def g1 6)
(def h1 7)
(def a2 8)
(def b2 9)
(def c2 10)
(def d2 11)
(def e2 12)
(def f2 13)
(def g2 14)
(def h2 15)
(def a3 16)
(def b3 17)
(def c3 18)
(def d3 19)
(def e3 20)
(def f3 21)
(def g3 22)
(def h3 23)
(def a4 24)
(def b4 25)
(def c4 26)
(def d4 27)
(def e4 28)
(def f4 29)
(def g4 30)
(def h4 31)
(def a5 32)
(def b5 33)
(def c5 34)
(def d5 35)
(def e5 36)
(def f5 37)
(def g5 38)
(def h5 39)
(def a6 40)
(def b6 41)
(def c6 42)
(def d6 43)
(def e6 44)
(def f6 45)
(def g6 46)
(def h6 47)
(def a7 48)
(def b7 49)
(def c7 50)
(def d7 51)
(def e7 52)
(def f7 53)
(def g7 54)
(def h7 55)
(def a8 56)
(def b8 57)
(def c8 58)
(def d8 59)
(def e8 60)
(def f8 61)
(def g8 62)
(def h8 63)

(defn rank [sqr]
  {:pre  [(<= a1 sqr h8)]
   :post [(<= 0 % 7)]}
  (quot sqr 8))

(defn file [sqr]
  {:pre  [(<= a1 sqr h8)]
   :post [(<= 0 % 7)]}
  (rem sqr 8))

(defn abs [x]
  (if (< x 0) (- x) x))

(defn rank-distance [sqr1 sqr2]
  (abs (- (rank sqr1) (rank sqr2))))

(defn file-distance [sqr1 sqr2]
  (abs (- (file sqr1) (file sqr2))))

(defn distance [sqr1 sqr2]
  (max (rank-distance sqr1 sqr2) (file-distance sqr1 sqr2)))

(defn on-board? [sqr]
  (and (>= sqr a1) (<= sqr h8)))

(defn over-the-edge? [from to]
  (or (not (on-board? to)) (not= 1 (distance from to))))

(def all-squares (range a1 (inc h8)))

(def rank-names {0 "1" 1 "2" 2 "3" 3 "4" 4 "5" 5 "6" 6 "7" 7 "8"})

(def file-names {0 "a" 1 "b" 2 "c" 3 "d" 4 "e" 5 "f" 6 "g" 7 "h"})

(defn sqr-to-str [sqr] (str (file-names (file sqr)) (rank-names (rank sqr))))

(defn sqrs-to-str [sqrs] (map sqr-to-str sqrs))