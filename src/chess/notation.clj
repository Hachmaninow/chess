(ns chess.notation
  (:require [chess.board :refer :all]))

(defn move-to-str [move]
  (let [piece (get-in move [:piece-movements 0]) from (to-square (get move :origin)) to (to-square (get-in move [:piece-movements 1]))]
    (str (when (not= :P (piece-type piece)) (name piece)) (name from) "-" (name to)
         )))


GAME := TAG_SECTION MOVE_TEXT
TAG_SECTION := TAG*

MOVE_TEXT := MOVE_NUMBER | MOVE_WITH_CHECK

MOVE_WITH_CHECK := MOVE '+'

MOVE := SIMPLE_PAWN_MOVE | SIMPLE_PIECE_MOVE

SIMPLE_PAWN_MOVE := FILE RANK '+'?
SIMPLE_PIECE_MOVE := PIECE FILE RANK

CASTLING_SHORT := 'O-O' | '0-0'
CASTLING_LONG := 'O-O-O' | '0-0-0'



MOVE_NUMBER := /d+ '.'

