(ns chessdojo.notation
  (:require [chessdojo.rules :as cr]
            [clojure.zip :as zip]
            [clojure.string :as string]
            ))

; standard algebraic notation
; https://chessprogramming.wikispaces.com/Algebraic+Chess+Notation

(def rank-names {0 "1" 1 "2" 2 "3" 3 "4" 4 "5" 5 "6" 6 "7" 7 "8"}) ; TODO: investigate (int \a) not supported in cljs (???)

(def file-names {0 "a" 1 "b" 2 "c" 3 "d" 4 "e" 5 "f" 6 "g" 7 "h"}) ; TODO: investigate (int \1) not supported in cljs (???)

(defn san [{:keys [:piece :from :to :disambig-file :disambig-rank :disambig-square :capture :castling :ep-capture :promote-to]}]
  (cond
    (nil? piece) "<error>"
    castling (name castling)
    :else (str
            (when (not= (cr/piece-type piece) :P) (name (cr/piece-type piece)))
            (when (and (= (cr/piece-type piece) :P) (or capture ep-capture)) (file-names (cr/file from)))
            (when (not= (cr/piece-type piece) :P) (get file-names disambig-file))
            (when (not= (cr/piece-type piece) :P) (get rank-names disambig-rank))
            (when (and disambig-square (not= (cr/piece-type piece) :P)) (name (cr/to-sqr disambig-square)))
            (when (or capture ep-capture) "x")
            (name (cr/to-sqr to)) (if ep-capture " e.p.")
            (if promote-to (str "=" (name (cr/piece-type promote-to)))))))

(defn- variation->str [variation-vec focussed-move]
  (string/join " " (map #(cond
                           (vector? %) (str "(" (variation->str % focussed-move) ")")
                           (:move %) (str
                                       (when (identical? (:move %) focussed-move) ">")
                                       (san (:move %)))
                           ) variation-vec)))

(defn notation [game]
  (variation->str (rest (zip/root game)) (:move (zip/node game)))) ; skip the first element which is the start position
