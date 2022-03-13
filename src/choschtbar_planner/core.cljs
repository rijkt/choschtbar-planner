(ns choschtbar-planner.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [chan]]
            ["moment" :as moment]
            ["moment/locale/de-ch" :as locale] ; import side effect: enables locale
            ["react-big-calendar" :refer (momentLocalizer)]
            [accountant.core :as accountant]
            [clojure.core.match :refer (match)]
            [choschtbar-planner.auth :as auth]
            [choschtbar-planner.calendar]
            [choschtbar-planner.shifts :as shifts]
            [choschtbar-planner.shift-detail]
            [choschtbar-planner.admin]))

(defonce app-state (atom {:selected nil})) ; todo: split up

(defn nav-bar [logout admin?]
  [:nav
   [:ul.flex.flex-row.justify-evenly.md:justify-end.my-5
    [:li.text-sm.font-sans.font-semibold.hover:text-green-500.mx-5.cursor-pointer
     {:onClick #(accountant/navigate! "/")} "Meine Touren"]
    [:li.text-sm.font-sans.font-semibold.hover:text-green-500.mx-5.cursor-pointer
     "Alle Touren"]
    (when admin?
      [:li.text-sm.font-sans.font-semibold.hover:text-green-500.mx-5.cursor-pointer
       {:onClick #(accountant/navigate! "admin")} "Administration"])
    [:li.text-sm.font-sans.font-semibold.hover:text-green-500.mx-5.cursor-pointer
     {:onClick logout} "Logout"]]     
   [:hr]])

(defn main []
  [:div
   [nav-bar auth/logout! (auth/is-admin (:id_token @auth/auth-state))]
   [:div.mt-6
    (match [(:path @app-state)]
           [(:or nil "/")] [choschtbar-planner.calendar/cal (:shifts @shifts/state)
                            (:localizer @app-state)
                            #(swap! app-state assoc :selected %)]
           ["detail"] (choschtbar-planner.shift-detail/detail (get (:shifts @shifts/state)
                                                                   (:id (:selected @app-state))))
           ["admin"] [choschtbar-planner.admin/root (:access_token @auth/auth-state)]
           :else [:p "404"])]])

(defn start []
  (moment/locale "de-CH") ; set up locale configuration
  (swap! app-state assoc :localizer (momentLocalizer moment)) ; save configured localizer
  (auth/authenticate!)
  (let [access-token-chan (chan 1)]
    (auth/get-token! access-token-chan)
    (shifts/initial-fetch! access-token-chan))
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
