(ns fetch-fx
  "fetch-fx provides an easy way to use `fetch` with a lightweight wrapper.
   Everything is focused around simplicity and try to reproduce the options of
   `fetch` to avoid cognitive overload, but of course by adding this little
   ClojureScript touch."
  (:require [clojure.string :as string]))

(defn body->str [body]
  (->> body clj->js (.stringify js/JSON)))

(defn params->str [params]
  (when (not= 0 (count params))
    (->> params
         (map (fn [[key value]] (str (name key) "=" value)))
         (string/join "&"))))

(defn generate-js-request [result method body]
  (if (not= :get method)
    (assoc result :body body)
    result))

(defn request->js [request]
  (let [body (body->str (:body request))]
    (-> request
        (dissoc :uri :params :body :json)
        (generate-js-request (:method request) body)
        clj->js)))

(defn uri->url [uri params]
  (if params
    (str uri "?" params)
    uri))

(defn text->json [json? text]
  (if json?
    (-> (.parse js/JSON text)
        (js->clj :keywordize-keys true))
    text))

(defn dispatch-response [message json?]
  (fn [response]
    (-> (.text response)
        (.then
         (fn [text]
           (let [value (text->json json? text)]
             (when message (rf/dispatch [message value response]))
             [value response]))))))

(defn fetch!
  "The options are the same as `fetch`, with some more options.
   `:uri`, `:json` and `:params` can be added.
   `:on-success` and `:on-failure` can be added too in case you’re working with
   re-frame."
  [request]
  (let [{:keys [uri on-success on-failure params]} request
        json? (:json request)
        options (request->js request)
        url (uri->url uri (params->str params))]
    (-> (js/fetch url options)
        (.then (dispatch-response on-success json?))
        (.catch (dispatch-response on-failure json?)))))
