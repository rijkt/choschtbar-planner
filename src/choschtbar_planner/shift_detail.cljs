(ns choschtbar-planner.shift-detail
  (:require [reagent.core :as reagent :refer [atom]]
            [accountant.core :as accountant]
            ["moment" :as moment]
            ["@heroicons/react/outline" :as icons]))

(defn detail [shift]
  (let [{:keys [id month startTime endTime color volunteer location notes]} shift
        start (moment/unix startTime)
        end (moment/unix endTime)]
    [:div.flex.flex-col.mx-5
     [:button.absolute.right-0.md:right-10 {:on-click #(accountant/navigate! "/")}
      [(reagent/adapt-react-class icons/XIcon) {:className "h-10 w-10"}]]
     [:p.text-lg.font-semibold.tracking-wide.my-5 (str location " - " notes)]
     [:div.flex.place-content-evenly
      [:p.text-base.my-3 (str (.format start "dddd, D.M."))]
      [:p.my-3 (str (.format start "HH:mm") " bis " (.format end "HH:mm"))] ; todo: add volunteers
      ]
     (if (or volunteer (seq volunteer))
       [:button.bg-blue-500.hover:bg-blue-700.text-white.font-bold.rounded.py-2.px-4.md:px-10
        {:type "button" :on-click #(js/alert "Dankeschön!")}
        [:p "Freiwillig melden"]]
       [:button.bg-red-500.hover:bg-red-700.text-white.font-bold.rounded.py-2.px-4
        {:type "button" :on-click #(js/alert "Schade!")}
        [:p "Abmelden"]])])) ; redirect to another page, confirm there
