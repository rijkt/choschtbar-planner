(ns choschtbar-planner.shifts
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! go]]
            [reagent.core :as reagent :refer [atom]]))

(defonce state (atom {:shifts {}}))

(defn initial-fetch! [access-token]
  (go
    (let [api "https://hybndamir4.execute-api.eu-central-1.amazonaws.com/default/getShifts"]
      (->> (<! (http/post api {:with-credentials? false :oauth-token access-token}))
           :body ; array of shifts
           (map (fn [shift] [(:id shift) shift]))
           (into (:shifts @state))
           (swap! state assoc :shifts)))))

(defn by-id [id shifts]
      (filter #(= (:volunteer %) id) (vals shifts)))
