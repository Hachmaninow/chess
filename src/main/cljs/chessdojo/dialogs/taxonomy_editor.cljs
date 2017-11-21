(ns chessdojo.dialogs.taxonomy-editor
  (:require [reagent.core :as reagent]
            [chessdojo.gateway :as gateway]))

(enable-console-print!)

(def new-taxon {:_id (random-uuid) :name ""})

(defn- init-taxon
  ([taxon] (init-taxon taxon (:parent taxon)))
  ([taxon parent-id] (assoc taxon :parent parent-id)))

(def current-taxon
  (reagent/atom nil))

(defn update-current-taxon-name [control]
  (swap! current-taxon assoc :name (-> control .-target .-value)))


(defn render-taxon [taxon]
  ^{:key (:_id taxon)}
  [:div.taxonomy-level {:style {:clear "both"}}
   [:span.pull-left (:name taxon) " [" (:_id taxon) "]"]
   [:button.btn.btn-link.pull-right {:href "#taxon-editor" :data-toggle "modal" :on-click #(reset! current-taxon (init-taxon new-taxon (:_id taxon)))} "Create sub taxon"]
   [:button.btn.btn-link.pull-right {:href "#taxon-editor" :data-toggle "modal" :on-click #(reset! current-taxon (init-taxon taxon))} "Edit"]
   (when (:children taxon) (map render-taxon (:children taxon)))])

(defn render-main []
  [:div#taxonomy-editor.modal.fade {:role "dialog"}
   [:div.modal-dialog {:role "document" :style {:width 1000 :height 1000}}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} "×"]]
      [:h4.modal-title "Edit taxonomy"]
      [:div.well {:style {:height 450}}
       (map render-taxon @chessdojo.state/taxonomy)]
      [:div.modal-footer
       [:a.btn.btn-primary {:href "#taxon-editor" :data-toggle "modal" :on-click #(reset! current-taxon (init-taxon new-taxon nil))} "New main taxon"]
       [:button.btn.btn-primary {:type "button" :data-dismiss "modal"} "Close"]]]]]])


(defn render-edit-taxon []
  [:div#taxon-editor.modal.fade {:role "dialog"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} "×"]]
      [:h4.modal-title "Edit taxon"]
      [:form
       [:div.form-group
        [:label {:for "taxon-parent-id"} "Parent"]
        [:input#taxon-parent-id.form-control {:type "text" :read-only true :value (str (:parent @current-taxon))}]]
       [:div.form-group
        [:label {:for "taxon-name"} "Name"]
        [:input#taxon-name.form-control {:type "text" :value (:name @current-taxon) :on-change update-current-taxon-name}]]]
      [:div.modal-footer
       [:button.btn.btn-default {:type "button" :data-dismiss "modal"} "Cancel"]
       [:button.btn.btn-primary {:type "button" :data-dismiss "modal" :on-click #(gateway/save-taxon @current-taxon)} "Ok"]]]]]])



(defn open-taxonomy-editor-button []
  [:button.btn.btn-default
   {:type "button" :data-toggle "modal" :data-target "#taxonomy-editor"}
   [:span.glyphicon.glyphicon-tree-conifer]])


