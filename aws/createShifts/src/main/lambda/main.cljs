(ns lambda.main
  (:require
   ["@aws-sdk/client-s3" :refer (S3Client GetObjectCommand PutObjectCommand)]
   ["node-fetch" :refer (Response)]
   [cljs.core.async :refer [go <! >! chan put!]]
   [cljs.core.async.interop :refer-macros [<p!]]))

(def ^js client (S3Client. (clj->js {:region "eu-central-1"})))

(defn make-response [shift] 
  (clj->js {:statusCode 201
            :body       (js/JSON.stringify (clj->js shift))
            :headers    {"Content-Type" "application/json"
                         "Access-Control-Allow-Origin" "*"}}))

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

(defn put-db [update update-c]
  (let [body (js/JSON.stringify (clj->js update))
        cmd-input #js {"Bucket" "choschtbar-data" "Key" "db.json" "Body" body}
        cmd (PutObjectCommand. cmd-input)]
    (go
      (prn (js/JSON.stringify cmd-input))
      (>! update-c (-> (.send client cmd)
                       (<p!)))))) ; don't parse the response for now

(defn handler [event]
  (let [to-create (get-body event) ; todo: add validation
        response-c (chan 1)
        response-promise (wrap-as-promise response-c)
        db-c (chan 1)
        update-c (chan 1)]
    (get-db db-c)
    (prn to-create)
    (go
      (let [update (update-in (<! db-c) [:shifts (:id to-create)] conj to-create)]
        (put-db update update-c)
        (prn (<! update-c))
        (>! response-c (make-response to-create))))
    response-promise)) ; make it an async function handler to avoid callback hell
