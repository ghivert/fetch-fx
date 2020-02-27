(ns fetch-fx.core
  "fetch-fx provides an easy way to use `fetch` with a lightweight wrapper.
   Everything is focused around simplicity and try to reproduce the options of
   `fetch` to avoid cognitive overload, but of course by adding this little
   ClojureScript touch."
  (:require [clojure.string :as string]
            [re-frame.core :as rf]))

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
        (dissoc :uri :params :body)
        (dissoc :json :array-buffer :blob :form-data)
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

(defn convert-headers [response]
  (->> (.-headers response)
       (.entries)
       (es6-iterator-seq)
       (reduce (fn [acc [key value]] (assoc acc (keyword key) value)) {})))

(defn convert-response [response]
  {:ok (.-ok response)
   :redirected (.-redirected response)
   :headers (convert-headers response)
   :status (.-status response)
   :status-text (.-statusText response)
   :type (.-type response)
   :url (.-url response)})

(defn dispatch-response [message body-extractor]
  (fn [response]
    (if (instance? js/TypeError response)
      (rf/dispatch [message (.-message response) response])
      (-> (body-extractor response)
          (.then #(js->clj % :keywordize-keys true))
          (.then
           (fn [value]
             (when message (rf/dispatch [message value (convert-response response) response]))
             [value (convert-response response) response]))))))

(defn select-body-extractor [{:keys [json array-buffer blob form-data]}]
  (cond
    json #(.json %)
    array-buffer #(.arrayBuffer %)
    blob #(.blob %)
    form-data #(.formData %)
    :else #(.text %)))

(defn fetch!
  "The options are the same as `fetch`, with some more options.
   `:uri`, `:json` and `:params` can be added.
   `:on-success` and `:on-failure` can be added too in case you’re working with
   re-frame."
  [request]
  (let [{:keys [uri on-success on-failure params]} request
        options (request->js request)
        url (uri->url uri (params->str params))]
    (-> (js/fetch url options)
        (.then (dispatch-response on-success (select-body-extractor request)))
        (.catch (dispatch-response on-failure (select-body-extractor request))))))
