(ns chess.pgn 
  (:require [instaparse.core :as insta]))

(def pgn
  (insta/parser
    "game = move-text
     <move-text> = (token space | token Epsilon)+
     <token> = move-number | black-move-number | move
     <space> = <#'\\s+'>
     move-number = #'\\d+' <#'\\.'>
     black-move-number = #'\\d+' <#'\\.\\.\\.'>
     <move> = (simple-pawn-move | capturing-pawn-move | simple-piece-move | capturing-piece-move | castling)
     <check-or-mate> = (check | mate)?
     simple-pawn-move = file rank check-or-mate
     capturing-pawn-move = file <'x'> file rank check-or-mate
     simple-piece-move = piece file rank check-or-mate
     capturing-piece-move = piece <'x'> file rank check-or-mate
     <castling> = (short-castling | long-castling) check-or-mate
     short-castling = <'O-O'>
     long-castling = <'O-O-O'>
     <piece> = 'K' | 'Q' | 'R' | 'B' | 'N' 
     <file> = 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h'
     <rank> = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8'    
     check = <'+'>
     mate = <'#'>
    "))

(pgn "1. e4 d6 2. d4 Nf6 3. Nc3 g6")

(pgn "7... b5 8. e5 b4 9. exf6 bxc3 10. bxc3 exf6 11. Bd3 Qe7+ 12. Ne2 Qf8 13. Qd2 Qe7 14. O-O O-O")

(pgn "34. axb4 Kxb4 35. Qc3+ Ka4 36. Qa3#")
