(ns choschtbar-planner.users
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]))

(defonce state (atom {:users []}))

(defn initial-fetch! [access-token]
  (go
    (let [api "https://hybndamir4.execute-api.eu-central-1.amazonaws.com/default/list-users"]
      (->> (<! (http/post api {:with-credentials? false :oauth-token access-token}))
           :body ; array of users
           (into (:users @state))
           (swap! state assoc :users)))))

