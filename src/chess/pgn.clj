(ns chess.pgn 
  (:require [instaparse.core :as insta]))

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
     <simple-piece-move> = piece to-file to-rank
     <capturing-piece-move> = piece capture to-file to-rank
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

(defn parse-move [move-string]
  (into {} (filter vector? (first (pgn move-string)))))

(defn move-matcher [{:keys [:castling :piece :to-file :to-rank]}]
  (remove nil?  
    (vector
      (when castling (fn [move] (= (move :castling) (keyword castling))))
      (when (and to-file to-rank) (fn [move] (= (move :to) (keyword (apply str to-file to-rank)))))
    )))

(defn matches? [move-string move]
  (every? #(% move) (move-matcher (parse-move move-string))))

(matches? "a6" {:to :a6})
(matches? "a5" {:to :a6})
(matches? "O-O" {:to :g1 :from :e1 :castling :O-O})
(matches? "O-O-O" {:to :g1 :from :e1 :castling :O-O})


