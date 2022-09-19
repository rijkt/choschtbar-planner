(ns choschtbar-planner.shifts
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! go]]
            [reagent.core :as reagent :refer [atom]]))

(defonce state (atom {:shifts {}}))

(def base-url
  "https://hybndamir4.execute-api.eu-central-1.amazonaws.com/default/")

(defn fetch-shifts! [access-token & month]
  (go
    (let [api (str base-url "getShifts")]
      (->> (<! (http/post api
                          {:with-credentials? false
                           :oauth-token access-token
                           :query-params month})) ; nil values do not get added
           :body ; array of shifts
           (map (fn [shift] [(:id shift) shift]))
           (into (:shifts @state))
           (swap! state assoc :shifts)))))

(defn by-id [id shifts]
  (filter #(= (:volunteer %) id) (vals shifts)))
