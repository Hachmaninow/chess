(ns chessdojo.pgn
  (:require [chessdojo.game :refer [soak]]
            [instaparse.core :as insta]
            [instaparse.failure]))

(def pgn
  (insta/parser
    "<game> = tags move-text
     <move-text> = (token space | token Epsilon)+
     <token> = move-number | black-move-number | move | comment | variation | annotation | game-result
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

     <tag-key> = #'[a-zA-Z]+'
     <tag-value> = <'\"'> #'[^\"]+' <'\"'>
     tag = <'['> tag-key space tag-value <']'>
     <tags> = (tag space)*

     comment = <'{'> #'[^}]+' <'}'>

     variation = <'('> space? move-text <')'>

     annotation = <'$'> #'[0-9]+'

     game-result = '1-0' | '1/2-1/2' | '0-1'
    "))

(defn parse-move [move]
  (if (or (= move :back) (= move :forward) (= move :out))   ; TODO: clean up redundancy; cf. game/navigate
    move
    (into {} (rest (first (pgn (name move)))))))

;
; pgn to event seq
;

(defn tokens->events [tokens])

(defn token->event [token]
  (condp = (first token)
    :move (into {} (rest token))                            ; [:move [:to-file "d"] [:to-rank "4"]] -> {:to-file "d" :to-rank "4"}
    :variation [:back (tokens->events (rest token)) :out :forward]
    nil
    ))

(defn tokens->events [tokens]
  (remove nil? (flatten (map token->event tokens))))

(defn pgn->events [game-str]
  (let [parse-tree (pgn game-str)]
    (if (insta/failure? parse-tree)
      (throw (ex-info "Invalid pgn input" {:parse-tree (insta/get-failure parse-tree)}))
      (tokens->events parse-tree))))

(defn load-pgn [pgn]
  (soak (pgn->events pgn)))