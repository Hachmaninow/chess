(ns chessdojo.views.browser
  (:require
    [chessdojo.gateway :as gateway]
    [chessdojo.state :as cst]
    [chessdojo.dialogs.import-inbox-editor :as import-inbox-editor]
    [chessdojo.dialogs.taxonomy-editor :as taxonomy-editor]))

(defn browser []
  [:div.panel.panel-default
   [:div.panel-heading
    [:span.button-group.pull-right
     [taxonomy-editor/open-taxonomy-editor-button]]]])
