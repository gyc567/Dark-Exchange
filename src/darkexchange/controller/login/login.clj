(ns darkexchange.controller.login.login
  (:require [clojure.contrib.logging :as logging]
            [darkexchange.controller.actions.utils :as actions-utils]
            [darkexchange.controller.login.create-user :as create-user]
            [darkexchange.controller.main.main-frame :as main-frame]
            [darkexchange.controller.utils :as controller-utils]
            [darkexchange.core :as core]
            [darkexchange.model.user :as user-model]
            [darkexchange.view.login.login :as login-view]
            [seesaw.core :as seesaw-core]))

(defn find-user-name-combobox [login-frame]
  (seesaw-core/select login-frame ["#user-name-combobox"]))

(defn reload-user-name-combobox [login-frame]
  (seesaw-core/config! (find-user-name-combobox login-frame) :model (user-model/all-user-names))
  login-frame)

(defn load-data [login-frame]
  (reload-user-name-combobox login-frame))

(defn attach-new-user-action [login-frame]
  (actions-utils/attach-listener login-frame "#new-user-button" (fn [_] (create-user/show login-frame))))

(defn attach-cancel-action [login-frame]
  (actions-utils/attach-window-close-and-exit-listener login-frame "#cancel-button"))

(defn attach-user-add-listener [login-frame]
  (user-model/add-user-add-listener (fn [_] (reload-user-name-combobox login-frame)))
  login-frame)

(defn find-password-field [login-frame]
  (seesaw-core/select login-frame ["#password-field"]))

(defn password [login-frame]
  (.getPassword (find-password-field login-frame)))

(defn reset-password [login-frame]
  (.setText (find-password-field login-frame) ""))

(defn login-fail [login-frame]
  (seesaw-core/alert "You entered an invalid user name or password. Please reenter them and try again.")
  (reset-password login-frame)
  (controller-utils/enable-widget login-frame))

(defn login-success [login-frame]
  (core/init)
  (main-frame/show)
  (actions-utils/close-window login-frame))

(defn login-setup [e login-frame]
  (if-let [user-name (seesaw-core/selection (find-user-name-combobox login-frame))]
    [login-frame user-name (password login-frame)]
    (seesaw-core/alert "You must select a user name. If no user exists, please create one.")))

(defn login [[login-frame user-name password]]
  [login-frame (user-model/login user-name password)])

(defn login-cleanup [[login-frame logged-in?]]
  (if logged-in?
    (login-success login-frame)
    (login-fail login-frame)))

(defn attach-login-action [login-frame]
  (actions-utils/attach-background-listener login-frame "#login-button"
    { :other-params login-frame
      :before-background login-setup
      :background login
      :after-background login-cleanup }))

(defn attach [login-frame]
  (attach-cancel-action (attach-login-action (attach-new-user-action (attach-user-add-listener login-frame)))))

(defn show []
  (controller-utils/show (attach (load-data (login-view/create)))))