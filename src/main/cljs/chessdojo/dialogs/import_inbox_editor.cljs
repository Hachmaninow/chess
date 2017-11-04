(ns chessdojo.dialogs.import-inbox-editor
  (:require [goog.string :as gstring]
            [chessdojo.state :as cst]
            [chessdojo.game :as cg]
            [reagent.core :as reagent]))

(def current-value
  (reagent/atom ""))

(defn update-current-value [control]
  (reset! current-value (-> control .-target .-value)))

(defn render []
  [:div#import-inbox-editor.modal.fade {:tab-index "-1" :role "dialog"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} (gstring/unescapeEntities "&times;")]]
      [:h4.modal-title "Import PGN to inbox "]
      [:textarea.full-width {:rows 10 :value (str @current-value) :on-change update-current-value}]
      [:div.modal-footer
       [:button.btn.btn-default {:type "button" :data-dismiss "modal"} "Cancel"]
       [:button.btn.btn-primary {:type     "button" :data-dismiss "modal"
                                 :on-click #(chessdojo.gateway/import-to-inbox @current-value)} "Ok"]]]]]])

(defn open-import-inbox-editor-button []
  [:button.btn.btn-default
   {:type "button" :data-toggle "modal" :data-target "#import-inbox-editor"}
   [:span.glyphicon.glyphicon-cloud-upload]])
