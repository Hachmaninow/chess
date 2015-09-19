(ns chess.pgn
  (:require [instaparse.core :as insta]
            [chess.board :refer :all]))

(def pgn
  (insta/parser
    "<game> = move-text
     <move-text> = (token space | token Epsilon)+
     <token> = move-number | black-move-number | move
     <space> = <#'\\s+'>
     move-number = #'\\d+' <#'\\.'>
     black-move-number = #'\\d+' <#'\\.\\.\\.'>
     move = (simple-pawn-move | capturing-pawn-move | simple-piece-move | capturing-piece-move | castling) call?
     <simple-pawn-move> = to-file to-rank
     <capturing-pawn-move> = from-file capture to-file to-rank
     <simple-piece-move> = piece from-file? from-rank? to-file to-rank
     <capturing-piece-move> = piece from-file? from-rank? capture to-file to-rank
     castling = (short-castling | long-castling)
     <short-castling> = 'O-O'
     <long-castling> = 'O-O-O'
     capture = 'x'
     piece = 'K' | 'Q' | 'R' | 'B' | 'N' 
     to-file = 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h'
     from-file = 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h'
     to-rank = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8'    
     from-rank = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8'    
     call = '+' | '#'
    "))

(pgn "1. e4 d6 2. d4 Nf6 3. Nc3 g6")

(pgn "7... b5 8. e5 b4 9. exf6 bxc3 10. bxc3 exf6 11. Bd3 Qe7+ 12. Ne2 Qf8 13. Qd2 Qe7 14. O-O O-O")

(pgn "34. axb4 Kxb4 35. Qc3+ Ka4 36. Qa3#")


(defn parsed-move-to-hash [parsed-move]
  (into {} (filter vector? parsed-move)))

(defn parse-move-text [move-text]
  (map parsed-move-to-hash (filter #(= (first %) :move) (pgn move-text))))

(defn parse-move [move-text]
  (first (parse-move-text move-text)))

(defn move-matcher [{:keys [:castling :piece :to-file :to-rank :capture :from-file :from-rank]}]
  (remove nil?
          (vector
            (when castling (fn [move] (= (move :castling) (keyword castling))))
            (when (and to-file to-rank) (fn [move] (= (move :to) (to-idx (keyword (apply str to-file to-rank))))))
            (when piece (fn [move] (= (keyword piece) (piece-type (move :piece))))) ; if a piece is specified it could be either black or white
            (when (and (not piece) (not castling)) (fn [move] (= (piece-type (move :piece)) :P))) ; if no piece is specified, then it is a pawn move (or a castling)
            (when capture (fn [move] (not (nil? (move :capture)))))
            (when from-file (fn [move] (= (- (int (first from-file)) (int \a)) (file (move :from)))))
            (when from-rank (fn [move] (= (- (int (first from-rank)) (int \1)) (rank (move :from)))))
            )))

(defn matches-parsed-move? [parsed-move move]
  (every? #(% move) (move-matcher parsed-move)))

