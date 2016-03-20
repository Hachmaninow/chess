(ns chessdojo.browser-test
  (:require [doo.runner :refer-macros [doo-tests]]
            [chessdojo.rules-test]
            [chessdojo.game-test]
            [chessdojo.data-test]
            [chessdojo.fen-test]
            [chessdojo.notation-test]))

(doo-tests 'chessdojo.rules-test 'chessdojo.game-test 'chessdojo.data-test 'chessdojo.fen-test 'chessdojo.notation-test)