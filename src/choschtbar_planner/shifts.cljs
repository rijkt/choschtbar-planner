(ns choschtbar-planner.shifts
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]))

(defonce state (atom {:shifts {}}))

(defn initial-fetch! [access-token-chan]
  (go
    (let [access-token (<! access-token-chan)
          api "https://hybndamir4.execute-api.eu-central-1.amazonaws.com/default/getShifts"]
      (->> (<! (http/post api {:with-credentials? false :oauth-token access-token}))
           :body ; array of shifts
           (map (fn [shift] [(:id shift) shift]))
           (into (:shifts @state))
           (swap! state assoc :shifts)))))
