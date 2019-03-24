(ns bard-tweets.generator
  (:require [overtone.at-at :as overtone]
            [twitter.api.restful :as twitter]
            [twitter.oauth :as twitter-oauth]
            [environ.core :refer [env]]))

(defn word-seq [word-transitions]
 (reduce (fn [r t] (merge-with clojure.set/union r
                               (let [[a b c] t]
                                 {[a b] (if c #{c} #{})})))
         {} word-transitions))

(defn part->word-seq [s]
  ( let [word (clojure.string/split s #"[\s|\n]")
         word-transitions (partition-all 3 1 word)]
    (word-seq word-transitions)))

(defn chain->part [chain]
  ( apply str (interpose " " chain)))


(defn walk-seq [prefix chain result]
  (let [suffixes (get chain prefix)]
    (if (empty? suffixes)
      result
      (let [new-suffix (first (shuffle suffixes))
            new-prefix [(last prefix) new-suffix]
            result-spaces (chain->part result)
            result-count (count result-spaces)
            suffix-count (inc(count new-suffix))
            total-count (+ suffix-count result-count)]
        (if (>= total-count 140)
          result
          (recur new-prefix chain (conj result new-suffix)))))))


(defn process-file [fname]
  (part->word-seq
    (slurp (clojure.java.io/resource fname))))


(def files ["e-a-poe.txt" "m-frye.txt" "p-neruda.txt" "potato.txt" "w-e-henly.txt"])

(def functional-potato(apply merge-with clojure.set/union (map process-file files)))



(defn generate-text
  [start-phrase word-chain]
  (let [prefix (clojure.string/split start-phrase #" ")
        result-chain (walk-seq prefix word-chain prefix)
        result-text (chain->part result-chain)]
    result-text))


(def prefix-list [ "I am" "Potato is" "From the"
                  "I have" "As it" "From the"
                  "And the" "I love" "Do not"
                  "My heart" "I shall" "In my"
                  "If I" "At the" "Under the"
                  "And yet" "The potato" "when the"
                  "To the" "As if" "I shall"
                  "Look for" "I want" "They are"
                  "I look" "If I" "From the"
                  "They also" "On that"])




(defn end-at-last-punctuation [text]
  (let [trimmed-to-last-punct (apply str (re-seq #"[\s\w]+[^.!?,]*[.!?,]" text))
        trimmed-to-last-word (apply str (re-seq #".*[^a-zA-Z]+" text))
        result-text (if (empty? trimmed-to-last-punct)
                      trimmed-to-last-word
                      trimmed-to-last-punct)
        cleaned-text (clojure.string/replace result-text #"[,| ]$" ".")]
    (clojure.string/replace cleaned-text #"\"" "'")))


(defn tweet-text []
  (let [text (generate-text (-> prefix-list shuffle first) functional-potato)]
    (end-at-last-punctuation text)))


(def creds (twitter-oauth/make-oauth-creds (env :app-consumer-key)
                                              (env :app-consumer-secret)
                                              (env :user-access-token)
                                              (env :user-access-secret)))

(defn status-update []
  (let [tweet (tweet-text)]
    (println "generated tweet is :" tweet)
    (println "char count is:" (count tweet))
    (when (not-empty tweet)
      (try (twitter/statuses-update :oauth-creds creds
                                    :params {:status tweet})
           (catch Exception e (println "Oh no! " (.getMessage e)))))
    ))

(def my-pool (overtone/mk-pool))

(defn -main [& args]
  ;; every 12 hours
  (println "Started up")
  (println (tweet-text))
  (overtone/every (* 1000 60 60 12) #(println (status-update)) my-pool))

