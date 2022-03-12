(ns choschtbar-planner.calendar
  (:require-macros [cljs.core.async.macros :refer [go]]) ; for cljs-http
  (:require [reagent.core :as reagent :refer [atom]]
            ["react-big-calendar" :refer (Calendar)]
            ["moment" :as moment]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [accountant.core :as accountant]))

(defn- to-event [shift]
  (let [note (:notes shift)
        location (:location shift)
        title (if note (str location  " - " note) location)
        id (:id shift)
        color (:color shift)
        start (.toDate (moment/unix (:startTime shift)))
        end (.toDate (moment/unix (:endTime shift)))
        volunteer (:volunteer shift)]
    {:id id :title title :color color :start start :end end :volunteer volunteer}))

(defn- make-event-style [event start end isSelected]
  (let [event (js->clj event :keywordize-keys true)
        bg-color (:color event)
        volunteer (:volunteer event)]
    (clj->js {:style (if volunteer {:backgroundColor bg-color}
                         {:backgroundColor "red" :borderStyle "dashed solid" :borderWidth 1})})))

(defn- select-event [dispatch]
  #(do
     (dispatch (js->clj %1 :keywordize-keys true))
     (accountant/navigate! "detail")))

(defn cal [shifts dispatch-shifts localizer dispatch-selected access-token]
  (let [mobile? (.-matches (js/window.matchMedia "(max-width: 600px)"))]
    (go (let [api "https://hybndamir4.execute-api.eu-central-1.amazonaws.com/default/getShifts"]
          (->>  (<! (http/post api {:with-credentials? false :oauth-token access-token}))
                :body ; array of shifts
                (map (fn [shift] [(:id shift) shift]))
                (into shifts)
                (dispatch-shifts))))
    (fn [shifts dispatch-shifts localizer dispatch-selected access-token]
      (let [events (map to-event (vals shifts))
            messages {:month "Monat" :today "Heute" :previous nil :next nil ; replaced with icons in css
                      :date "Datum" :time "Zeit" :event "Tour"}]
        [(reagent/adapt-react-class Calendar) {:localizer localizer :events events
                                               :style {:height 700} ; bind default view to media query
                                               :views {:month true :agenda true}
                                               :defaultView (if mobile? "agenda" "month")
                                               :messages messages :selectable true
                                               :onSelectEvent (select-event dispatch-selected)
                                               :eventPropGetter make-event-style}]))))
