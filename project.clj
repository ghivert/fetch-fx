(defproject fetch-fx "0.1.0"
  :description
  "fetch-fx provides an easy way to use `fetch` with a lightweight wrapper.
   Everything is focused around simplicity and try to reproduce the options of
   `fetch` to avoid cognitive overload, but of course by adding this little
   ClojureScript touch."
  :url "https://github.com/ghivert/fetch-fx"
  :license {:name "MIT"
            :url "https://github/ghivert/fetch-fx/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojurescript "1.10.520" :scope "provided"]
                 [re-frame "0.11.0" :scope "provided"]]
  :source-paths ["src"]
  :deploy-repositories
  [["releases" {:sign-releases false
                :url "https://clojars.org/repo"}]])
