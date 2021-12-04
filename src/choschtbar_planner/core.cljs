(ns choschtbar-planner.core
  (:require [reagent.core :as reagent :refer [atom]]
            ["react-big-calendar" :refer (Calendar momentLocalizer)]
            ["moment" :as moment]))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!" :localizer (momentLocalizer moment)}))


(defn hello-world []
  [:div
    [:h1.text-4xl.mt-2.font-normal (:text @app-state)]
    [:h3 "Edit this and watch it change!"]
    [(reagent/adapt-react-class Calendar) {:localizer (:localizer @app-state) :events []}]])

(defn start []
  (reagent/render-component [hello-world]
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
