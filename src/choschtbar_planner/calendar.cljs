(ns choschtbar-planner.calendar
  (:require [choschtbar-planner.auth :as auth]
            [choschtbar-planner.shifts :as shifts]
            [reagent.core :as reagent :refer [atom]]
            ["react-big-calendar" :refer (Calendar)]
            ["moment" :as moment]
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

(defn cal [shifts localizer dispatch-selected]
  (let [mobile? (.-matches (js/window.matchMedia "(max-width: 600px)"))
        events (map to-event shifts)
        messages {:month "Monat" :today "Heute" :previous nil :next nil ; replaced with icons in css
                  :date "Datum" :time "Zeit" :event "Tour"}]
    [:div
     [:svg.h-6.w-6.cursor-pointer
      {:on-click #(shifts/fetch-shifts! (auth/read-token))
       :xmlns "http://www.w3.org/2000/svg"
       :fill "none"
       :viewBox "0 0 24 24"
       :stroke "currentColor"
       :stroke-width "2"}
      [:path {:stroke-linecap "round"
              :stroke-linejoin "round"
              :d "M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"}]]
      
     [(reagent/adapt-react-class Calendar) {:localizer localizer :events events
                                           :style {:height 700} ; bind default view to media query
                                           :views {:month true :agenda true}
                                           :defaultView (if mobile? "agenda" "month")
                                           :messages messages :selectable true
                                           :onSelectEvent (select-event dispatch-selected)
                                           :eventPropGetter make-event-style}]]
    ))
