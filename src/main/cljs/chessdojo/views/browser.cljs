(ns chessdojo.views.browser
  (:require
    [reagent.core :as reagent]
    [chessdojo.gateway :as gateway]
    [chessdojo.state :as cst]
    [chessdojo.dialogs.taxonomy-editor :as taxonomy-editor]))

(defn new-taxon []  {:_id (random-uuid) :name ""})

(defn- init-taxon
  ([taxon] (init-taxon taxon (:parent taxon)))
  ([taxon parent-id] (assoc taxon :parent parent-id)))

(defn- render-single-taxon [taxon]
  ^{:key (:_id taxon)}
  [:div.taxonomy-level {:style {:clear "both"}}
   [:span.pull-left (:name taxon) " [" (:_id taxon) "]"
    [:button.btn.btn-link.pull-right
     {:href "#taxon-editor" :data-toggle "modal" :on-click #(reset! cst/current-taxon (init-taxon (new-taxon) (:_id taxon)))} "Create sub taxon"]
    [:button.btn.btn-link.pull-right
     {:href "#taxon-editor" :data-toggle "modal" :on-click #(reset! cst/current-taxon (init-taxon taxon))} "Edit"]
    (when (:children taxon) (map render-single-taxon (:children taxon)))
   ]])

(defn browser []
  [:div
   [:a {:href "#taxon-editor" :data-toggle "modal" :on-click #(reset! cst/current-taxon (init-taxon (new-taxon) nil) )} "New root taxon"]
   [:hr]
   (map render-single-taxon @cst/taxonomy)])
