(ns chessdojo.views.board
  (:require
    [reagent.core :as reagent]))

(defn render-chessground []
  [:div#chessground.chessground.merida])

(defn chessground-mounted [this]
  (js/Chessground
    (reagent/dom-node this)
    (clj->js {:orientation "white"})))

(defn chessground []
  (reagent/create-class {:render              render-chessground
                         :component-did-mount chessground-mounted}))

(defn board []
  [:div.blue
   [chessground]])
