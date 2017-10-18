(ns chessdojo.views.browser
  (:require
    [cljsjs.react-bootstrap]
    [reagent.core :refer [atom]]))

(defn listed-game-view [game]
  (let [id (:_id game)
        {white :White black :Black result :Result opening :Opening} (:game-info game)]
    ^{:key id} [:tr                                         ;{:on-click #(load-game id)}
                [:td white]
                [:td black]
                [:td result]
                [:td opening]]))

(defn inbox-view [game-list]
  [:table.table.table-striped.table-hover.small

   [:tbody
    (for [game @game-list]
      (listed-game-view game))]])

(defn browser [game-list]
  [:div
   [inbox-view game-list]])

