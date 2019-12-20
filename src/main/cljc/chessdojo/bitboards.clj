(ns chessdojo.bitboards
  (:require [chessdojo.squares :refer :all]))

(defn- bb-row [bb row]
  (bit-and (bit-shift-right bb (* row 8)) 0xFF))

(defn- bb-row-to-str [row]
  (clojure.string/join
    (for [x (range 0 8)] (if (bit-test row x) "x " "· "))))

(defn bb-to-str [bb]
  (clojure.string/join "\n"
                       (for [row (range 7 -1 -1)] (bb-row-to-str (bb-row bb row)))))

;(def a-file 0x0101010101010101)
;(def b-file (bit-shift-left a-file 1))
;(def c-file (bit-shift-left b-file 1))
;(def d-file (bit-shift-left c-file 1))
;(def e-file (bit-shift-left d-file 1))
;(def f-file (bit-shift-left e-file 1))
;(def g-file (bit-shift-left f-file 1))
;(def h-file (bit-shift-left g-file 1))
;
;(def rank-1 0xFF)
;(def rank-2 (bit-shift-left rank-1 8))
;(def rank-3 (bit-shift-left rank-2 8))
;(def rank-4 (bit-shift-left rank-3 8))
;(def rank-5 (bit-shift-left rank-4 8))
;(def rank-6 (bit-shift-left rank-5 8))
;(def rank-7 (bit-shift-left rank-6 8))
;(def rank-8 (bit-shift-left rank-7 8))

(defn set-sqr [sqr]
  (bit-set 0 sqr))

(defn set-sqrs [sqrs]
  (reduce bit-or 0 (map set-sqr sqrs)))

(defn cond-sqrs [cond range]
  (reduce bit-or (map #(if (cond %) (set-sqr %) 0) range)))

(defn has-distance [to dist]
  (fn [from] (= (distance from to) dist)))

(defn neighbors-1 [sqr]
  (cond-sqrs (has-distance sqr 1) all-squares))

(defn neighbors-2 [sqr]
  (bit-or (neighbors-1 sqr)
          (cond-sqrs (has-distance sqr 2) all-squares)))

(def knight-bearings [-17 -15 -10 -6 +6 +10 +15 +17])

(defn knight-attacks [sqr]
  (let [knight-targets (map (partial + sqr) knight-bearings)]
    (bit-and (neighbors-2 sqr)
             (set-sqrs knight-targets))))

(defn scan [bb]
  (filter (partial bit-test bb) all-squares))

(defn ls1b [bb]
  (if (zero? bb)
    0
    (loop [bit 1
           sqr a1]
      (if-not (zero? (bit-and bit bb))
        sqr
        (recur (bit-shift-left bit 1) (inc sqr))))))

(defn ms1b [bb]
  (if (zero? bb)
    0
    (loop [bit (bit-shift-left 1 63)
           sqr h8]
      (if-not (zero? (bit-and bit bb))
        sqr
        (recur (bit-shift-right bit 1) (dec sqr))))))

(def directions
  {:N  {:step 8 :first-blocker-fn ls1b}
   :S  {:step -8 :first-blocker-fn ms1b}
   :W  {:step -1 :first-blocker-fn ms1b}
   :E  {:step 1 :first-blocker-fn ls1b}
   :NE {:step 9 :first-blocker-fn ls1b}
   :SE {:step -7 :first-blocker-fn ms1b}
   :SW {:step -9 :first-blocker-fn ms1b}
   :NW {:step 7 :first-blocker-fn ls1b}})

;(defn target-squares [sqr dir]
;  (let [step (steps dir) limit (if (pos? step) 64 -1)]
;    (for [tgt (range (+ sqr step) limit step) :while (= 1 (distance tgt (- tgt step)))] tgt))
;  )
;
;(defn ray-attacks [sqr dir]
;  (reduce bit-or 0 (map #(only %) (target-squares sqr dir))))

;(defn neg-ray-attacks [sqr dir occupied]
;  (let [attacked (ray-attacks sqr dir)
;        blocked (bit-and attacked occupied)]
;    (if-not (zero? blocked)
;      (let [first-blocker (ms1b blocked)]
;        (bit-xor attacked (ray-attacks first-blocker dir)))
;      attacked)))

(defn ray-attacks-open [sqr dir]
  (let [step-size (get-in directions [dir :step])]
    (loop [from sqr
           to (+ sqr step-size)
           bb 0]
      (if (over-the-edge? from to)
        bb
        (recur to (+ to step-size) (bit-or bb (set-sqr to)))))))

(defn ray-attacks [sqr dir occupied]
  (let [first-blocker-fn (get-in directions [dir :first-blocker-fn])
        attacked (ray-attacks-open sqr dir)
        blocked (bit-and attacked occupied)]
    (if-not (zero? blocked)
      (let [first-blocker (first-blocker-fn blocked)]
        (bit-xor attacked (ray-attacks-open first-blocker dir)))
      attacked)))

(defn rook-attacks [sqr occupied]
  (apply bit-or (map #(ray-attacks sqr % occupied) [:N :S :W :E])))

(defn bishop-attacks [sqr occupied]
  (apply bit-or (map #(ray-attacks sqr % occupied) [:NE :SE :SW :NW])))

(defn queen-attacks [sqr occupied]
  (bit-or (rook-attacks sqr occupied) (bishop-attacks sqr occupied)))
