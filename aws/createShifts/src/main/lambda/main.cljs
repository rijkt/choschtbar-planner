(ns lambda.main
  (:require
   ["@aws-sdk/client-s3" :refer (S3Client GetObjectCommand)]
   ["node-fetch" :refer (Response)]
   [cljs.core.async :refer [go <! >! chan put!]]
   [cljs.core.async.interop :refer-macros [<p!]]))

(def ^js client (S3Client. (clj->js {:region "eu-central-1"})))

(defn make-response [shift] 
  (clj->js {:statusCode 201
            :body       (js/JSON.stringify (clj->js shift))
            :headers    {"Content-Type" "application/json"}}))

(defn get-body [event]
  (-> event
      (js->clj :keywordize-keys true)
      :body
      (js/JSON.parse)
      (js->clj :keywordize-keys true)
      (assoc :id (str (random-uuid)))))

(defn wrap-as-promise
  [channel]
  (new js/Promise (fn [resolve _] 
                    (go (resolve (<! channel))))))

(defn get-db [channel]
  (let [cmd-input (clj->js {:Bucket "choschtbar-data" :Key "db.json"})
        cmd (GetObjectCommand. cmd-input)]
    (go
      (put! channel (-> (.send client cmd)
                        (<p!)
                        (.-Body) ; IncomingMessage : stream.Readable
                        (Response.)
                        (.json)
                        (<p!)
                        (js->clj :keywordize-keys true))))))

(defn handler [event]
  (let [to-create (get-body event) ; todo: add validation
        response-c (chan 1)
        response-promise (wrap-as-promise response-c)
        db-c (chan 1)]
    (get-db db-c)
    (prn to-create)
    (go
      (prn (<! db-c))
      (>! response-c (make-response to-create)))
    response-promise)) ; make it an async function handler to avoid callback hell
