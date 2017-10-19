(ns chessdojo.state
  (:require
    [reagent.core :as reagent]))

(def game-list
  (reagent/atom nil))

(def buffers
  (reagent/atom nil))