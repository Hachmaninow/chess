(ns chessdojo.state
  (:require
    [reagent.core :as reagent]
    [chessdojo.game :as cg]
    [clojure.zip :as zip]))

(def game-list
  (reagent/atom nil))

(def scratch-buffer {:id "_scratch" :game (cg/with-game-info cg/new-game {"Title" "Scratch"})})

(def buffers
  (reagent/atom [scratch-buffer]))

(def main-buffer
  (reagent/atom scratch-buffer))

(defn current-game []
  (:game @main-buffer))

(defn update-game [new-game]
  (swap! main-buffer assoc :game new-game))

(defn current-node []
  (zip/node (:game @main-buffer)))

