(ns choschtbar-planner.core
  (:require [reagent.core :as reagent :refer [atom]]
            ["moment" :as moment]
            ["moment/locale/de-ch" :as locale] ; import side effect: enables locale
            ["react-big-calendar" :refer (momentLocalizer)]
            [choschtbar-planner.calendar]))

(defonce app-state (atom {:shifts []}))

(defn main []
  (choschtbar-planner.calendar/cal {:shifts (:shifts @app-state) :localizer (:localizer @app-state)}))

(defn start []
  (moment/locale "de-CH") ; set up locale configuration
  (swap! app-state assoc :localizer (momentLocalizer moment)) ; save configured localizer
  (reagent/render-component [main]
                            (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
