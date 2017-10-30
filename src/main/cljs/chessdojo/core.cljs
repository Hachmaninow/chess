(ns chessdojo.core
  (:require [chessdojo.views.layout :as layout]
            [chessdojo.views.browser :as browser]))

(enable-console-print!)

(defn init! []
  (layout/mount-grid)
  (layout/mount-dialogs)
  (browser/load-game-list)
  ;(fetch-taxonomy)
  )
