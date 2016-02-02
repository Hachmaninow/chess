(ns chessdojo.browser-test
  (:require [doo.runner :refer-macros [doo-tests]]
            [chessdojo.rules-test]
            [chessdojo.game-test]
            [chessdojo.data-test]
            ))

(doo-tests 'chessdojo.rules-test 'chessdojo.game-test 'chessdojo.data-test)