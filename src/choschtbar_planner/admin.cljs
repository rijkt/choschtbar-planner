(ns choschtbar-planner.admin
  (:require [reagent.core :as reagent :refer [atom]]))

(defn- assoc-for [key state]
  (fn [e] (swap! state assoc key (-> e .-target .-value))))

(defn root []
  (let [s (atom {})]
    (fn []
      [:form.grid.grid-cols-2.gap-2 {:on-submit (fn [e] (.preventDefault e))}
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
       [:select {:name :volunteer :required true}
        [:option "Maxine Muster"]]
       [:label {:for :color} "Farbe"] ; todo: preselected values
       [:input {:type :color :name :color :required true :on-change (assoc-for :color s)}]
       [:input {:type :submit :name :create :value "Schicht eintragen"}]])))
