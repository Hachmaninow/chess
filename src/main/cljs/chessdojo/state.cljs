(ns chessdojo.state
  (:require
    [reagent.core :as reagent]
    [chessdojo.game :as cg]
    [clojure.zip :as zip]))

(def game-list
  (reagent/atom nil))


; buffer handling

; start with a single scratch buffer to begin with
(def active-buffer-id
  (reagent/atom (str "scratch-" (random-uuid))))

(def buffers
  (reagent/atom {@active-buffer-id {:game cg/new-game}}))

(defn switch-active-buffer [id]
  (reset! active-buffer-id id))

(defn open-buffer [id game]
  (do
    (println (str "opening buffer: " id))
    (swap! buffers assoc id {:game game})))

(defn active-buffer []
  (get @buffers @active-buffer-id))


; handling of current game

(defn active-game []
  (:game (active-buffer)))

(defn update-game [updated-game]
  (swap! buffers assoc @active-buffer-id {:game updated-game}))

(defn active-node []
  (zip/node (:game (active-buffer))))

