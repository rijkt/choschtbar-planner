(ns choschtbar-planner.auth
  (:require [cemerick.url :refer (url url-encode)]
            [cljs.core.async :refer [go chan <!]]
            [cljs-http.client :as http]))

(defonce auth-state (atom {}))

(defn- read-code! []
  (-> js/window
      .-location
      .-href
      (url)
      :query
      (get "code")))

(defn authenticate! []
  (when (and (nil? (:auth @auth-state))
             (nil? (read-code!)))
    (set! (.. js/window -location -href)
          "https://choschtbar-planner.auth.eu-central-1.amazoncognito.com/oauth2/authorize?client_id=381pjf00caq7m17j2js95ko00b&response_type=code&scope=aws.cognito.signin.user.admin+email+openid+phone+profile&redirect_uri=http%3A%2F%2Flocalhost%3A8700%2F")))

(defn logout! []
  (swap! auth-state assoc :auth nil)
  (set! (.. js/window -location -href)
        "https://choschtbar-planner.auth.eu-central-1.amazoncognito.com/logout?client_id=381pjf00caq7m17j2js95ko00b&&logout_uri=http%3A%2F%2Flocalhost%3A8700%2F"))

(defn get-token!
  "Call token endpoint if code url param is present"
  []
  (when-let [code (read-code!)]
    (go
      (let [token-url "https://choschtbar-planner.auth.eu-central-1.amazoncognito.com/oauth2/token"
            form-params  {:grant_type "authorization_code"
                          :code code
                          :client_id "381pjf00caq7m17j2js95ko00b"
                          :redirect_uri "http://localhost:8700/"}
            response-chan (chan 1 (map :body))]
        (http/post token-url {:form-params form-params :with-credentials? false :channel response-chan})
        (swap! auth-state assoc :auth (<! response-chan)))))) ; todo: flatten?
