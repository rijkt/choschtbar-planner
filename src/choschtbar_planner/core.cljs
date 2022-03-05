(ns choschtbar-planner.core
  (:require [reagent.core :as reagent :refer [atom]]
            ["moment" :as moment]
            ["moment/locale/de-ch" :as locale] ; import side effect: enables locale
            ["react-big-calendar" :refer (momentLocalizer)]
            [accountant.core :as accountant]
            [clojure.core.match :refer (match)]
            [cemerick.url :refer (url url-encode)]
            [cljs.core.async :refer [go chan <!]]
            [cljs-http.client :as http]
            [choschtbar-planner.calendar]
            [choschtbar-planner.shift-detail]
            [choschtbar-planner.admin]))

(defonce app-state (atom {:shifts {} :selected nil})) ; todo: split up

(defn main []
  [:div
   [:nav
    [:ul.flex.flex-row.justify-evenly.md:justify-end.my-5
     [:li.text-sm.font-sans.font-semibold.hover:text-green-500.mx-5.cursor-pointer
      {:onClick #(accountant/navigate! "/")} "Meine Touren"]
     [:li.text-sm.font-sans.font-semibold.hover:text-green-500.mx-5.cursor-pointer
      "Alle Touren"]
     [:li.text-sm.font-sans.font-semibold.hover:text-green-500.mx-5.cursor-pointer
      {:onClick #(accountant/navigate! "admin")} "Administration"]
     [:li.text-sm.font-sans.font-semibold.hover:text-green-500.mx-5.cursor-pointer
      "Logout"]
     [:a {:href "https://choschtbar-planner.auth.eu-central-1.amazoncognito.com/oauth2/authorize?client_id=381pjf00caq7m17j2js95ko00b&response_type=code&scope=aws.cognito.signin.user.admin+email+openid+phone+profile&redirect_uri=http%3A%2F%2Flocalhost%3A8700%2F"} "Login"]]
    [:hr]]
   [:div.mt-6
    (match [(:path @app-state)]
           [(:or nil "/")] [choschtbar-planner.calendar/cal (:shifts @app-state) #(swap! app-state assoc :shifts %)
                            (:localizer @app-state) #(swap! app-state assoc :selected %)]
           ["detail"] (choschtbar-planner.shift-detail/detail (get (:shifts @app-state) (:id (:selected @app-state))))
           ["admin"] [choschtbar-planner.admin/root]
           :else [:p "404"])]])

(defn get-token!
  "Call token endpoint if code url param is present"
  []
  (when-let [code (-> js/window
                      .-location
                      .-href
                      (url)
                      :query
                      (get "code"))]
    (go
      (let [token-url "https://choschtbar-planner.auth.eu-central-1.amazoncognito.com/oauth2/token"
            form-params  {:grant_type "authorization_code"
                          :code code
                          :client_id "381pjf00caq7m17j2js95ko00b"
                          :redirect_uri "http://localhost:8700/"}
            response-chan (chan 1 (map :body))]
        (http/post token-url {:form-params form-params :with-credentials? false :channel response-chan})
        (swap! app-state assoc :auth (<! response-chan))))))

(defn start []
  (moment/locale "de-CH") ; set up locale configuration
  (swap! app-state assoc :localizer (momentLocalizer moment)) ; save configured localizer
  (get-token!)
  (accountant/configure-navigation!
   {:nav-handler (fn [path] (swap! app-state assoc :path path))
    :path-exists? (fn [path] true)}) ; todo: integrate bidi
  (reagent/render-component [main]
                            (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
