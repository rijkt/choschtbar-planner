(ns lambda.main
  (:require
   [cljs.core.async :refer [go <! >! chan]]
   [cljs.core.async.interop :refer-macros [<p!]]
   ["@aws-sdk/client-cognito-identity-provider"
    :refer (CognitoIdentityProviderClient, ListUsersCommand)]))

(def ^js client (CognitoIdentityProviderClient.
                 (clj->js {:region "eu-central-1"})))

(def user-pool-id "eu-central-1_X4Hn8hjzp")

(defn make-response [data] 
  (clj->js {:statusCode 200
            :body       (js/JSON.stringify (clj->js data))
            :headers    {"Content-Type" "application/json"
                         "Access-Control-Allow-Origin" "*"}}))

(defn wrap-as-promise
  [channel]
  (new js/Promise (fn [resolve _] 
                    (go (resolve (<! channel))))))

(defn extract-attrs [attrs]
  (->> attrs
       (map #(seq [(:Name %) (:Value %)]))
       (map (fn [[k v]] [(keyword k) v]))
       (into {})
       ))

(defn extract-users [users]
  (->> users
       (filter :Enabled)
       (filter #(= (:UserStatus %) "CONFIRMED"))
       (map #(merge {:Username (:Username %)} (extract-attrs (:Attributes %))))))

(defn log! [data message]
  (prn {:message message :data data})
  data)

(defn handler []
  (let [response-c (chan 1)
        response-promise (wrap-as-promise response-c)]
    (go
      (let [cmd (ListUsersCommand.
                 (clj->js {:UserPoolId user-pool-id}))]
        (>! response-c (-> (.send client cmd)
                           (.catch #(log! "got an error response" %))
                           (<p!)
                           (log! "got response")
                           (js->clj :keywordize-keys true)  ; todo: pagination
                           :Users
                           (extract-users)
                           (log! "extracted response")
                           (make-response)))))
    response-promise)) ; make it an async function handler to avoid callback hell
