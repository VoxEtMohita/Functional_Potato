(defproject bard-tweets "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [overtone/at-at "1.2.0"]
                 [twitter-api "1.8.0"]
                 [environ "1.1.0"]]
  :main bard-tweets.generator
  :min-lein-version "2.0.0"
  :plugins [[lein-environ "1.0.0"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.7.0"]]}}
  )




