(ns chessdojo.dialogs.taxonomy-editor
  (:require [reagent.core :as reagent]))

(def current-value
  (reagent/atom ""))

(defn update-current-value [control]
  (reset! current-value (-> control .-target .-value)))

(defn render-main []
  [:div#taxonomy-editor.modal.fade {:role "dialog"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} "×"]]
      [:h4.modal-title "Edit taxonomy"]
      [:a.btn.btn-primary {:href "#taxon-editor" :data-toggle "modal"} "New main taxon"]
      [:div.modal-footer
       [:button.btn.btn-primary {:type "button" :data-dismiss "modal"} "Close"]]]]]])

(defn save-taxon []
  )

(defn render-edit-taxon []
  [:div#taxon-editor.modal.fade {:role "dialog"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} "×"]]
      [:h4.modal-title "Edit taxon"]

      [:div.modal-footer
       [:button.btn.btn-default {:type "button" :data-dismiss "modal"} "Cancel"]
       [:button.btn.btn-primary {:type "button" :data-dismiss "modal" :on-click save-taxon} "Ok"]]]]]])



(defn open-taxonomy-editor-button []
  [:button.btn.btn-default
   {:type "button" :data-toggle "modal" :data-target "#taxonomy-editor"}
   [:span.glyphicon.glyphicon-tree-deciduous]])
