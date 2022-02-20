(ns lambda.main
  (:require
   ["@aws-sdk/client-s3" :refer (S3Client GetObjectCommand)]
   ["node-fetch" :refer (Response)]
   [cljs.core.async :refer [go <! >! chan]]
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

(defn handler [event context]
  (let [chanl (chan 1)
        promise (wrap-as-promise chanl)
        cmd-input (clj->js {:Bucket "choschtbar-data" :Key "db.json"})
        cmd (GetObjectCommand. cmd-input)]
    (go
      (let [response (<p! (.send client cmd))
            body (.-Body response ) ; IncomingMessage : stream.Readable
            res (Response. body)]
        (prn (<p! (.json res)))
        (>! chanl dummy)))
    promise)) ; make it an async function handler to avoid callback hell
