(ns chessdojo.state
  (:require
    [reagent.core :as reagent]
    [chessdojo.game :as cg]))

(def game-list
  (reagent/atom nil))

(def scratch-buffer {:id "scratch" :game cg/new-game})

(def buffers
  (reagent/atom [scratch-buffer]))

(def main-buffer
  (reagent/atom scratch-buffer))