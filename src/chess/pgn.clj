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
     <simple-pawn-move> = to-file to-rank promote-to?
     <capturing-pawn-move> = from-file capture to-file to-rank promote-to?
     <simple-piece-move> = piece from-file? from-rank? to-file to-rank
     <capturing-piece-move> = piece from-file? from-rank? capture to-file to-rank
     castling = (short-castling | long-castling)
     <short-castling> = 'O-O'
     <long-castling> = 'O-O-O'
     capture = 'x'
     piece = 'K' | 'Q' | 'R' | 'B' | 'N' 
     <promotion-piece> = 'Q' | 'R' | 'B' | 'N'
     to-file = 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h'
     from-file = 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h'
     to-rank = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8'    
     from-rank = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8'    
     call = '+' | '#'
     promote-to = <'='> promotion-piece
    "))

(defn parsed-move-to-hash [parsed-move]
  (into {} (filter vector? parsed-move)))

(defn parse-move-text [move-text]
  (map parsed-move-to-hash (filter #(= (first %) :move) (pgn move-text))))

(defn parse-move [move-text]
  (first (parse-move-text move-text)))

(defn move-matcher [{:keys [:castling :piece :to-file :to-rank :capture :from-file :from-rank :promote-to]}]
  (remove nil?
          (vector
            (when castling (fn [move] (= (move :castling) (keyword castling))))
            (when (and to-file to-rank) (fn [move]  (=  (move :to) (to-idx (keyword (apply str to-file to-rank))))))
            (when piece (fn [move] (= (keyword piece) (piece-type (move :piece))))) ; if a piece is specified it could be either black or white
            (when (and (not piece) (not castling)) (fn [move] (= (piece-type (move :piece)) :P))) ; if no piece is specified, then it is a pawn move (or a castling)
            (when capture (fn [move] (or (move :capture) (move :ep-capture))))
            (when from-file (fn [move] (= (- (int (first from-file)) (int \a)) (file (move :from)))))
            (when from-rank (fn [move] (= (- (int (first from-rank)) (int \1)) (rank (move :from)))))
            (when promote-to (fn [move] (= (keyword promote-to) (piece-type (move :promote-to)))))
            )))

(defn matches-parsed-move? [parsed-move move]
  (every? #(% move) (move-matcher parsed-move)))

