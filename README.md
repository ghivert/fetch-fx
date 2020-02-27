# Fetch FX

Fetch FX is a package aiming to provide an easy and lightweight wrapper around the native [`fetch API`](https://developer.mozilla.org/fr/docs/Web/API/WindowOrWorkerGlobalScope/fetch), but with the ClojureScript niceties added.

I’m not sure wether I should upload it to Clojars ([and join all](https://clojars.org/fetch-fx) [of the](https://clojars.org/day8.re-frame/fetch-fx) [`#".*fetch-fx.*"` projects](https://clojars.org/superstructor/re-frame-fetch-fx))… For now, if the project is interesting for you, grab it directly, it remains in two files, and let me a sweet comment to indicate that you’re interested in the project. I really don’t see the point to push another project with little or no added value and which only will pollute the global Clojars.

I’ll try to push the package further, so you can probably expect updates in a near future (as the time of writing those lines, the 25th February 2020).

# Getting Started

In your event handler, probably something like `events.cljs`, require `[fetch-fx.re-frame]`. Now, you can use them in `reg-event-fx`. It will be better with an example with a login.

```clojure
(ns your-cool-project.events
  (:require [re-frame.core :as rf :refer [reg-event-fx reg-event-db]]
            [fetch-fx.re-frame]))

(reg-event-fx :try-to-login
  (fn [cofx [_ email password]]
    {:fetch ; The fetch-fx is just :fetch in your fx map.
      {:method :post
       :json true ; :json can be used in order to convert the response from JSON into CLJ map.
       :array-buffer true
       :blob true
       :form-data true
       :body ; The body will automatically be converted into JSON.
         {:email email
          :password password}
       :uri "/login"
       :on-success :success-login
       :on-failure :failure-login}}))

(reg-event-db :success-login
  ; This event will be triggered if the request is successful.
  (fn [db [_ response]]
    (do-something-with-response response)))

(reg-event-db :failure-login
  ; This event will be triggered if the request is successful.
  (fn [db [_ error]
    (do-something-with-error error)]))
```

Look carefully at the code above. First we define the fx with the `:fetch` keyword. It accepts a map containing at least `:uri`, `:on-success` and `:on-failure`. All other fields are optional.  
Then we define two handlers: `:on-success` and `:on-failure`. They will be triggered respectively when the request success or fail.

`:json` will indicate that the response is in JSON format and should be translated right away into CLJ map.

`:body` is used only for POST and other requests accepting body.

You can also use directly `fetch-fx` without re-frame. The function allowing this is `fetch!`. It accepts the same map options as the re-frame fx.

```clojure
(ns your-cool-project
  (:require [fetch-fx.core :refer [fetch!]]))

(defn fetch-data!
  "This function will return a native JS Promise but the result will still be converted."
  []
  (fetch!
    {:method :post
     :json true
     :body {:email email
            :password password}
     :uri "/login"}))

(defn convert-data []
  (-> (fetch-data!)
      (.then (fn [[result]]
               (:username result)))))
```


## Response

```clojure
(def response
  {:ok "boolean"
  :redirected "boolean"
  :headers {:key "value"}
  :status 200
  :status-text "ok"
  :type "ok"
  :url "ok"})
```
