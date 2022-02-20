(ns lambda.main
  (:require
   ["@aws-sdk/client-s3" :refer (S3Client GetObjectCommand)]
   ["node-fetch" :refer (Response)]
   [cljs.core.async :refer [go <! >! chan put!]]
   [cljs.core.async.interop :refer-macros [<p!]]))

(def ^js client (S3Client. (clj->js {:region "eu-central-1"})))

(def dummy 
  (clj->js {:statusCode 200
            :body       "Created"
            :headers    {"Content-Type" "application/json"}}))

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

(defn handler [event context]
  (let [response-c (chan 1)
        response-promise (wrap-as-promise response-c)
        db-c (chan 1)]
    (get-db db-c)
    (go
      (prn (<! db-c))
      (>! response-c dummy))
    response-promise)) ; make it an async function handler to avoid callback hell
