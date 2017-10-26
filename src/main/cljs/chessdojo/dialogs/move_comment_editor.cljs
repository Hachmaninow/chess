(ns chessdojo.dialogs.move-comment-editor
  (:require [goog.string :as gstring]))

(defn render []
  [:div#move-comment-editor.modal.fade {:tab-index "-1" :role "dialog" :aria-labelledby "move-comment-editor-title"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} (gstring/unescapeEntities "&times;")]]
      [:h4#move-comment-editor-title.modal-title "Modal title"]
      [:div.modal-body "Modal title"
       [:p "body"]]
      [:div.modal-footer
       [:button.btn.btn-default {:type "button" :data-dismiss "modal"} "Close"]
       [:button.btn.btn-primary {:type "button"} "Save changes"]
       ]]]]])

(defn edit-move-comment-button []
  [:button.btn.btn-default {:type "button" :data-toggle "modal" :data-target "#move-comment-editor"}
   [:span.glyphicon.glyphicon-pencil]])
