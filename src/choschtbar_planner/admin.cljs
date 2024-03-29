(ns choschtbar-planner.admin
  (:require [choschtbar-planner.shifts :as shifts]
            [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [go chan <!]]
            [cljs-http.client :as http]
            ["moment" :as moment]))

(defn- assoc-for [key state]
  (fn [e] (swap! state assoc key (-> e .-target .-value))))

(defn- to-unix [date time]
  (.unix (moment (str date "T" time))))

(defn- submit [s access-token]
  (fn [e]
    (.preventDefault e)
                                        ; form-wide validation via e.target goes here
    (let [date (:date @s)
          month (.substring date 0 7) ; YYYY-MM
          body {:month month
                :startTime (to-unix date (:start-time @s))
                :endTime (to-unix date (:end-time @s)) 
                :color (:color @s)
                :volunteer (:volunteer @s)
                :location (:tour @s)
                :notes (:notes @s)}
          response-chan (chan 1 (map :status))]
      (go
        (swap! s assoc :response nil)
        (shifts/create-shift! body access-token response-chan)
        (swap! s assoc :response (<! response-chan))
        (shifts/fetch-shifts! access-token {:month month})))))

(defonce s (atom {}))

(defn root [access-token users]
  (fn [access-token users]
    [:form.grid.grid-cols-2.gap-2 {:on-submit (submit s access-token)}
     [:label {:for :tour} "Tour"]
     [:input.border-green-800.border-2.border-solid.rounded-md
      {:type :text :name :tour :required true :on-change (assoc-for :tour s)}]
     [:label {:for :notes} "Kommentar"]
     [:input.border-green-800.border-2.border-solid.rounded-md
      {:type :text :name :notes :required true :on-change (assoc-for :notes s)}]
     [:label {:for :date} "Datum"]
     [:input {:type :date :name :date :required true :on-change (assoc-for :date s)}]
     [:label {:for :start-time} "Von"]
     [:input {:type :time :name :start-time :required true :on-change (assoc-for :start-time s)}]
     [:label {:for :end-time} "Bis"]
     [:input {:type :time :name :end-time :required true :on-change (assoc-for :end-time s)}]
     [:label {:for :volunteer} "Freiwillige*r"]
     [:select {:name :volunteer :on-change (assoc-for :volunteer s)}
      [:option {:label "Nicht zugewiesen" :value nil}]
      (map (fn [user] [:option {:label (:nickname user) :value (:sub user)}]) users)]
     [:label {:for :color} "Farbe"] ; todo: preselected values
     [:input {:type :color :name :color :required true :on-change (assoc-for :color s)}]
     [:input {:type :submit :name :create :value "Schicht eintragen"}]
     [:div (condp = (:response @s)
           nil nil
           201 [:p.bg-green-500.text-center "Erfolgreich erfasst"]
           [:p.bg-red-500.text-center "Ein Fehler ist aufgetreten!"])]]))
