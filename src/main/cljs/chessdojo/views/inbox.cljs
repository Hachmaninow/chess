(ns chessdojo.views.inbox
  (:require
    [chessdojo.gateway :as gateway]
    [chessdojo.state :as cst]
    [reagent.core :as reagent]))

(enable-console-print!)

(def current-value
  (reagent/atom ""))

(defn update-current-value [control]
  (reset! current-value (-> control .-target .-value)))

(defn import-pgn []
  [:div
   [:textarea.full-width {:rows 20 :value (str @current-value) :on-change update-current-value}]
   [:button.btn.btn-primary.float-right.mt-1 {:type     "button"
                                              :on-click #(do (println "Importing" @current-value)
                                                             (chessdojo.gateway/import-to-inbox @current-value))} "Import"]])

(defn listed-game-view [game]
  (let [id (:_id game)
        {white :White black :Black result :Result} (:game-info game)]
    ^{:key id} [:tr {:on-click #(gateway/load-game id)}
                [:td id]
                [:td white]
                [:td black]
                [:td result]]))

(defn inbox-games []
  [:table.table.table-striped.table-hover.table-condensed.small
   [:tbody
    (for [game @cst/game-list]
      (listed-game-view game))]])