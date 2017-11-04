(ns chessdojo.core
  (:require [chessdojo.views.layout :as layout]
            [chessdojo.gateway :as gateway]))

(enable-console-print!)

(defn init! []
  (layout/mount-grid)
  (layout/mount-dialogs)
  (gateway/load-game-list)
  ;(fetch-taxonomy)
  )
