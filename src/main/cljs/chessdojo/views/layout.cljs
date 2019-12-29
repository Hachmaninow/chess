(ns chessdojo.views.layout
  (:require
    [chessdojo.state :as cst]
    [chessdojo.game :as cg]
    [chessdojo.views.board :refer [board]]
    [chessdojo.views.browser :refer [browser inbox-view]]
    [chessdojo.views.buffers :refer [buffers]]
    [chessdojo.views.editor :refer [editor]]
    [chessdojo.views.navbar :refer [navbar]]
    [chessdojo.dialogs.move-comment-editor]
    [chessdojo.dialogs.game-info-editor]
    [chessdojo.dialogs.study-metadata-editor]
    [chessdojo.dialogs.taxonomy-editor]
    [reagent.core :as reagent]))

(enable-console-print!)

(def active-tab
  (reagent/atom :board-with-editor))

(defn toggle-tab [activated-tab]
  (reset! active-tab activated-tab))

(defn open-study [id]
  (do
    (reset! active-tab :board-with-editor)
    (cst/switch-active-buffer id)))

(def browser-tab
  [:li.nav-item
   [:a#browser-tab {:on-click #(toggle-tab :browser) :class-name (if (= @active-tab :browser) "nav-link active" "nav-link")}
    [:i.material-icons "home"]]])

(defn- study-tab-name [{title :Title white :White black :Black}]
  (cond
    (some? title) title
    (or white black) (str white " - " black)
    :default "???"))

(defn- study-tab [id]
  (let [game (:game (get @cst/buffers id))
        game-info (cg/game-info game)]
    ^{:key id} [:li.nav-item
                [:a {:class-name (if (and (= @active-tab :board-with-editor) (= id @cst/active-buffer-id)) "nav-link active" "nav-link")
                     :on-click #(open-study id)
                     :style    {:padding "10px 5px"}} (study-tab-name game-info)]]))

(defn study-tabs []
  (doall
    (map study-tab (keys @cst/buffers))))

(defn grid-layout []
  [:div.container-fluid
   [:div.row
    [:div.col-12
     [:ul.nav.nav-tabs
      (cons browser-tab (study-tabs))]]]

   [:div#browser {:class-name (if (= @active-tab :browser) "row d-block" "d-none")}
    [browser]
    ]

   [:div#board-with-editor {:class-name (if (= @active-tab :board-with-editor) "row d-inline" "d-none")}
    [:div.col-8
     ;[buffers]
     [board]
     [navbar]
     ]
    [:div.col-4
     [editor]]]

   ])

(defn mount-grid []
  (reagent/render [grid-layout] (.getElementById js/document "mount")))

(defn dialogs []
  [:div
   [chessdojo.dialogs.move-comment-editor/render]
   [chessdojo.dialogs.game-info-editor/render]
   [chessdojo.dialogs.study-metadata-editor/render]
   [chessdojo.dialogs.import-inbox-editor/render]
   [chessdojo.dialogs.taxonomy-editor/render-main]
   [chessdojo.dialogs.taxonomy-editor/render-edit-taxon]])

(defn mount-dialogs []
  (reagent/render [dialogs] (.getElementById js/document "dialogs")))
