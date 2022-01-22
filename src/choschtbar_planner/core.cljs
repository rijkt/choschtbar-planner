(ns choschtbar-planner.core
  (:require-macros [cljs.core.async.macros :refer [go]]) ; for cljs-http
  (:require [reagent.core :as reagent :refer [atom]]
            ["react-big-calendar" :refer (Calendar momentLocalizer)]
            ["moment" :as moment]
            ["moment/locale/de-ch" :as locale] ; import for side effect
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

;; define your app data so that it doesn't get over-written on reload

(moment/locale "de-CH")

(defonce app-state (atom {:shifts [] :localizer (momentLocalizer moment)}))

(go (let [api "https://hybndamir4.execute-api.eu-central-1.amazonaws.com/default/getShifts"
          response (<! (http/post api {:with-credentials? false}))]
      (swap! app-state assoc :shifts (:body response))))

(defn to-event [shift]
  {:id (uuid (:id shift)) :title (str (:location shift) " - " (:notes shift)) :color (:color shift)
   :start (.toDate (moment/unix (:startTime shift))) :end (.toDate (moment/unix (:endTime shift)))
   :volunteer (:volunteer shift)})

(defn make-event-style [event start end isSelected]
  (let [event (js->clj event :keywordize-keys true)
        bg-color (:color event)
        volunteer (:volunteer event)]
    (clj->js {:style (if volunteer {:backgroundColor bg-color}
                         {:backgroundColor "red" :borderStyle "dashed solid" :borderWidth 1})})))

(defn main []
  [:div
   [:h1.text-4xl.mt-2.font-normal.mb-4 "Deine Touren"]
   (let [events (map to-event (:shifts @app-state))]
     [(reagent/adapt-react-class Calendar) {:localizer (:localizer @app-state) :events events
                                            :style {:height 500}
                                            :eventPropGetter make-event-style}])])

(defn start []
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
