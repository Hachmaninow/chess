(ns chessdojo.browser-test
  (:require [doo.runner :refer-macros [doo-tests]]
            [chessdojo.rules-test]
            [chessdojo.game-test]
            ))

(doo-tests 'chessdojo.rules-test 'chessdojo.game-test)