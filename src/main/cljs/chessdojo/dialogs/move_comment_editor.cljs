(ns chessdojo.dialogs.move-comment-editor
  (:require [goog.string :as gstring]
            [chessdojo.state :as cst]
            [chessdojo.game :as cg]
            [reagent.core :as reagent]))

(def current-value
  (reagent/atom ""))

(defn update-comment []
  (cst/update-game (cg/set-comment (cst/current-game) @current-value)))

(defn update-current-value [control]
  (reset! current-value (-> control .-target .-value)))

(defn render []
  [:div#move-comment-editor.modal.fade {:tab-index "-1" :role "dialog" :aria-labelledby "move-comment-editor-title"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} (gstring/unescapeEntities "&times;")]]
      [:h4#move-comment-editor-title.modal-title "Edit move comment"]
      [:textarea.full-width {:rows 10 :value (str @current-value) :on-change update-current-value}]
      [:div.modal-footer
       [:button.btn.btn-default {:type "button" :data-dismiss "modal"} "Cancel"]
       [:button.btn.btn-primary {:type "button" :data-dismiss "modal" :on-click update-comment} "Ok"]]]]]])

(defn init-current-value []
  (reset! current-value (:comment (cst/current-node))))

(defn edit-move-comment-button []
  [:button.btn.btn-default
   {:type "button" :data-toggle "modal" :data-target "#move-comment-editor" :on-click init-current-value}
   [:span.glyphicon.glyphicon-pencil]])
