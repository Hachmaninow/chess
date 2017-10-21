(ns chessdojo.views.board
  (:require
    [reagent.core :as reagent]
    [chessdojo.state :as cst]
    [chessdojo.fen :as cf]))

(enable-console-print!)

(defn options []
  (let [buffer @cst/main-buffer]
    {:fen         (cf/fen (:game buffer))
     :orientation "white"}
    )
  )

(defn render-chessground []
  [:div#chessground.chessground.merida])

(def chessground-board
  (reagent/atom nil))

(defn chessground-mounted [this]
  (reset! chessground-board
    (js/Chessground
      (reagent/dom-node this)
      (clj->js (options)))))

(defn chessground []
  (reagent/create-class
    {:render              render-chessground
     :component-did-mount chessground-mounted}))

(defn update-board []
  (when @chessground-board
    (.set @chessground-board (clj->js (options)))))

(defonce board-updater (reagent/track! update-board))

(defn board []
  [:div.blue
   [chessground]])
