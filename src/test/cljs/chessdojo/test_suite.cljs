(ns chessdojo.test-suite
  (:require [doo.runner :refer-macros [doo-tests]]
            [chessdojo.squares-test]
            [chessdojo.bitboards-test]
            [chessdojo.rules-test]
            [chessdojo.game-test]
            [chessdojo.data-test]
            [chessdojo.fen-test]
            [chessdojo.notation-test]
            [chessdojo.views.board-test]
            [chessdojo.dialogs.game-info-editor-test]))

(doo-tests
  'chessdojo.squares-test
  ;'chessdojo.bitboards-test
  'chessdojo.rules-test
  'chessdojo.game-test
  'chessdojo.data-test
  'chessdojo.fen-test
  'chessdojo.notation-test
  'chessdojo.views.board-test
  'chessdojo.dialogs.game-info-editor-test)