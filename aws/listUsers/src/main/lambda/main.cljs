(ns lambda.main
  (:require
   [cljs.core.async :refer [go <! >! chan]]))

(defn make-response [data] 
  (clj->js {:statusCode 201
            :body       (js/JSON.stringify (clj->js data))
            :headers    {"Content-Type" "application/json"
                         "Access-Control-Allow-Origin" "*"}}))

(defn wrap-as-promise
  [channel]
  (new js/Promise (fn [resolve _] 
                    (go (resolve (<! channel))))))

(defn handler []
  (let [response-c (chan 1)
        response-promise (wrap-as-promise response-c)]
    (go
      (let [dummy {:message "hello world"}]
        (>! response-c (make-response dummy))))
    response-promise)) ; make it an async function handler to avoid callback hell
