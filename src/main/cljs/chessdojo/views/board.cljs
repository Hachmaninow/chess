(ns chessdojo.views.board
  (:require
    [reagent.core :as reagent]
    [chessdojo.state :as cst]
    [chessdojo.fen :as cf]
    [chessdojo.rules :as cr]
    [clojure.zip :as zip]
    [chessdojo.game :as cg]))

(enable-console-print!)

(defn move-destinations [game]
  (let [position (:position (zip/node game))
        valid-moves (cr/valid-moves position)
        from-to (map #(select-keys % [:from :to]) valid-moves)
        grouped-by-from (group-by :from from-to)]
    (reduce-kv #(assoc %1 (cr/to-sqr %2) (map cr/to-sqr (map :to %3))) {} grouped-by-from)))

(defn insert-move [from to meta]
  (do
    (println (str "insert move: " from " " to " " (js->clj meta)))
    (let [buffer @cst/main-buffer
          game (:game buffer)
          move-coords {:from (cr/to-idx (keyword from)) :to (cr/to-idx (keyword to))}
          new-game (cg/insert-move game move-coords)]
      (cst/update-game new-game))))

(defn create-chessground-options []
  (let [buffer @cst/main-buffer
        game (:game buffer)]
    {:fen         (cf/fen game)
     :orientation "white"
     :movable     {
                   :free       false
                   :dests      (move-destinations game)
                   :show-dests true
                   :events     {
                                :after insert-move
                                }
                   }
     }
    )
  )

(defn render-chessground []
  [:div#chessground.chessground.merida.center-block])

(def chessground-board
  (reagent/atom nil))

(defn chessground-mounted [this]
  (reset! chessground-board
    (js/Chessground
      (reagent/dom-node this)
      (clj->js (create-chessground-options)))))

(defn chessground []
  (reagent/create-class
    {:render              render-chessground
     :component-did-mount chessground-mounted}))

(defn update-board []
  (when @chessground-board
    (.set @chessground-board (clj->js (create-chessground-options)))))

(defonce board-updater (reagent/track! update-board))

(defn board []
  [:div.blue
   [chessground]])
