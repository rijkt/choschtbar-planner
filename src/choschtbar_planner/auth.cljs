(ns choschtbar-planner.auth
  (:require [cemerick.url :refer (url)]
            [cljs.core.async :refer [go chan <! >!]]
            [cljs-http.client :as http]
            ["jwt-decode" :as jwt-decode]))

(defonce auth-state (atom {}))

(def authorization-url "https://choschtbar-planner.auth.eu-central-1.amazoncognito.com")

(goog-define REDIRECT-URI "http://localhost:8700/")

(def client-id "381pjf00caq7m17j2js95ko00b")

(defn- read-code! []
  (-> js/window
      .-location
      .-href
      (url)
      :query
      (get "code")))

(defn authenticate! []
  (when (and (nil? (:id_token @auth-state))
             (nil? (read-code!)))
    (set! (.. js/window -location -href)
          (str authorization-url "/oauth2/authorize?"
               "client_id=" client-id
               "&response_type=code"
               "&scope=aws.cognito.signin.user.admin+email+openid+phone+profile&"
               "redirect_uri=" REDIRECT-URI))))

(defn logout! []
  (swap! auth-state assoc :auth nil)
  (set! (.. js/window -location -href)
        (str authorization-url "/logout?client_id=" client-id "&logout_uri=" REDIRECT-URI)))

(defn get-token!
  "Call token endpoint if code url param is present"
  [access-token-c]
  (when-let [code (read-code!)]
    (go
      (let [token-url (str authorization-url "/oauth2/token")
            form-params  {:grant_type "authorization_code"
                          :code code
                          :client_id client-id
                          :redirect_uri REDIRECT-URI}
            response-chan (chan 1 (map :body))]
        (http/post token-url {:form-params form-params :with-credentials? false :channel response-chan})
        (let [response (<! response-chan)]
          (swap! auth-state merge response)
          (>! access-token-c (:access_token response))))))) ; todo: snake to kebab case

(defn is-admin [id-token]
  (when id-token
    (-> id-token
        jwt-decode
        (js->clj :keywordize-keys true)
        :cognito:groups
        (= ["Admin"]))))
