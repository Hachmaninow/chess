(ns chessdojo.views.layout
  (:require
    [chessdojo.state :as cst]
    [chessdojo.game :as cg]
    [chessdojo.views.board :refer [board]]
    [chessdojo.views.browser :refer [browser]]
    [chessdojo.views.inbox :refer [inbox]]
    [chessdojo.views.buffers :refer [buffers]]
    [chessdojo.views.editor :refer [editor]]
    [chessdojo.views.navbar :refer [navbar]]
    [chessdojo.dialogs.move-comment-editor]
    [chessdojo.dialogs.game-info-editor]
    [chessdojo.dialogs.study-metadata-editor]
    [chessdojo.dialogs.taxonomy-editor]
    [reagent.core :as reagent]))

;
; tabs
;

(defn- toggle-tab [activated-tab-or-id]
  (if (keyword? activated-tab-or-id)
    (reset! cst/active-tab activated-tab-or-id)             ; either specific named tab
    (do                                                     ; ...or some study with a specific id
      (reset! cst/active-tab :study)
      (cst/switch-active-buffer activated-tab-or-id))))

(defn- named-tab [name icon]
  [:li.nav-item {:id name}
   [:a {:on-click   #(toggle-tab name)
        :class-name (if (= @cst/active-tab name) "nav-link active" "nav-link")}
    [:i.material-icons icon]]])

(defn- study-tab-name [{title :Title white :White black :Black}]
  (cond
    (some? title) title
    (or white black) (str white " - " black)
    :default "???"))

(defn- study-tab [id]
  (let [game (:game (get @cst/buffers id))
        game-info (cg/game-info game)]
    ^{:key id} [:li.nav-item {:id (str "study-tab-" id)}
                [:a {:on-click   #(toggle-tab id)
                     :class-name (if (and (= @cst/active-tab :study) (= id @cst/active-buffer-id)) "nav-link active" "nav-link")}
                 (study-tab-name game-info)]]))

(defn study-tabs []
  (doall
    (map study-tab (keys @cst/buffers))))

;
; grid
;

(defn grid-layout []
  [:div.container
   [:div.row
    [:div.col-12
     [:ul.nav.nav-tabs
      [named-tab :browser "home"]
      [named-tab :inbox "inbox"]
      (study-tabs)

      ;(cons [browser-tab] (cons [inbox-tab] (study-tabs)))
      ;[inbox-tab browser-tab (study-tabs)]
      ;(cons inbox-tab (study-tabs))
      ]]]


   [:div#browser {:class-name (if (= @cst/active-tab :browser) "row" "d-none")}
    [browser]]

   [:div#inbox {:class-name (if (= @cst/active-tab :inbox) "row" "d-none")}
    [inbox]]

   [:div#board-with-editor {:class-name (if (= @cst/active-tab :study) "row " "d-none")}

    [:div#board.col-sm-7
     [board]
     [navbar]]

    [:div#editor.col-sm-5
     [editor]]
    ]

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
