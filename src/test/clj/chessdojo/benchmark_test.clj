(ns chessdojo.benchmark-test
  (:require [clojure.test :refer :all]
            [chessdojo.pgn :as pgn]
            [taoensso.timbre.profiling :as profiler]))

(defn game-benchmark [pgn-str]
  (pgn/load-pgn pgn-str))
; 3.3s

(deftest run
  (profiler/profile :info :Arithmetic
                    (dotimes [_ 3]
                      (game-benchmark (slurp "src/test/cljc/test-pgns/complete.pgn")))))

;(run)
