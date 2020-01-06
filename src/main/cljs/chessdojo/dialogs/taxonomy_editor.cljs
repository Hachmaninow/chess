(ns chessdojo.dialogs.taxonomy-editor
  (:require [reagent.core :as reagent]
            [chessdojo.gateway :as gateway]
            [chessdojo.state :as cst]))

(enable-console-print!)

(defn update-current-taxon-name [control]
  (swap! cst/current-taxon assoc :name (-> control .-target .-value)))

(defn render-edit-taxon []
  [:div#taxon-editor.modal.fade {:tab-index "-1" :role "dialog" :aria-labelledby "exampleModalLabel" :aria-hidden "true"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:h5#exampleModalLabel.modal-title "Edit Category"]
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden "true"} "×"]]]
     [:div.modal-body
      [:div.form-group
       [:label {:for "taxon-parent-id"} "Parent"]
       [:input#taxon-parent-id.form-control {:type "text" :read-only true :value (str (:parent @cst/current-taxon))}]]
      [:div.form-group
       [:label {:for "taxon-name"} "Name"]
       [:input#taxon-name.form-control {:type "text" :value (:name @cst/current-taxon) :on-change update-current-taxon-name}]]]
     [:div.modal-footer
      [:button.btn.btn-secondary {:type "button" :data-dismiss "modal"} "Close"]
      [:button.btn.btn-primary {:type "button" :on-click #(gateway/save-taxon @cst/current-taxon)} "Save changes"]]]]]
  )

(defn open-taxonomy-editor-button []
  [:button.btn.btn-default
   {:type "button" :data-toggle "modal" :data-target "#taxonomy-editor"}
   [:span.glyphicon.glyphicon-tree-conifer]])


