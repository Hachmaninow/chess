(ns chessdojo.dialogs.study-metadata-editor
  (:require [reagent.core :as reagent]
            [reagent-forms.core :refer [bind-fields init-field value-of]]))

(defn row [label input]
  [:div.row
   [:div.col-2 [:label label]]
   [:div.col-3 input]])

(defn input [label type id]
  (row label [:input.form-control {:field type :id id}]))

(def form
  [:div.card
   [:div.card-header "Study metadata"]
   [:div.card-body
    (input "Title" :text :study.title)
    (input "Subtitle" :text :study.subtitle)
    (input "White" :text :study.white)
    (input "Black" :text :study.black)
    (input "Event" :text :study.event)
    (input "Result" :text :study.result)]])

(def current-study
  (reagent/atom {:study {:title "Lucena position" :white "Nobody" :event "Linares" :result "1-0"}}))

(defn render []
  (fn []
    [:div
     [bind-fields form current-study]
     [:button.btn.btn-default {:on-click #(println @current-study)} "Save"]]))
