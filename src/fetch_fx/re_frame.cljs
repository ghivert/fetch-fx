(ns fetch-fx.re-frame
  "Provides the `:fetch` effect in re-frame."
  (:require [fetch-fx]
            [re-frame.core :as rf]))

(rf/reg-fx :fetch fetch-fx/fetch!)
