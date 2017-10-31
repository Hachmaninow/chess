(ns chessdojo.dialogs.game-info-editor
  (:require [goog.string :as gstring]
            [chessdojo.state :as cst]
            [chessdojo.game :as cg]
            [clojure.string :as string]
            [reagent.core :as reagent]))

(enable-console-print!)

; backing atom for state of modal
(def current-value
  (reagent/atom ""))

(defn str->game-info [game-info-str]
  (into {} (map #(string/split % "=") (string/split-lines game-info-str))))

(defn game-info->str [game-info]
  (string/join "\n" (map (fn [[k v]] (str (name k) "=" v)) game-info)))

(defn update-game-info []
  (do
    (cst/update-game (cg/with-game-info (cst/active-game) (str->game-info @current-value)))
    (println (cg/game-info (cst/active-game)))))

(defn update-current-value [control]
  (reset! current-value (-> control .-target .-value)))

(defn render []
  [:div#game-info-editor.modal.fade {:tab-index "-1" :role "dialog"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} (gstring/unescapeEntities "&times;")]]
      [:h4.modal-title "Edit game info"]
      [:textarea.full-width {:rows 10 :value (str @current-value) :on-change update-current-value}]
      [:div.modal-footer
       [:button.btn.btn-default {:type "button" :data-dismiss "modal"}
        "Cancel"]
       [:button.btn.btn-primary {:type "button" :data-dismiss "modal" :on-click update-game-info}
        "Ok"]]]]]])

(defn init-current-value []
  (let [game-info (cg/game-info (cst/active-game))]
    (reset! current-value (game-info->str game-info))))

(defn edit-game-info-button []
  [:button.btn.btn-default
   {:type "button" :data-toggle "modal" :data-target "#game-info-editor" :on-click init-current-value}
   [:span.glyphicon.glyphicon-tags]])
