(ns bard-tweets.generator-test
  (:require [clojure.test :refer :all]
            [bard-tweets.generator :refer :all]))

;;(def example "The unicorn and the potato went off to the beach and their brother the potato went to eat a sandwich")

(deftest test-word-seq
  (testing "produces a map of possible suffixes for a particular prefix"
    (let [ex '(("The" "unicorn" "and")
               ("unicorn" "and" "the")
               ("and" "the" "potato")
               ("the" "potato" "went")
               ("potato" "went" "off")
               ("went" "off" "to")
               ("off" "to" "the")
               ("to" "the" "beach")
               ("the" "beach" "and")
               ("beach" "and" "their")
               ("and" "their" "brother")
               ("their" "brother" "the")
               ("brother" "the" "potato")
               ("the" "potato" "went")
               ("potato" "went" "to")
               ("went" "to" "eat")
               ("to" "eat" "a")
               ("eat" "a" "sandwich")
               ("a" "sandwich")
               ("sandwich"))]
      (is (= {["and" "the"] #{"potato"},
              ["and" "their"] #{"brother"},
              ["their" "brother"] #{"the"},
              ["eat" "a"] #{"sandwich"},
              ["off" "to"] #{"the"},
              ["the" "beach"] #{"and"},
              ["to" "eat"] #{"a"},
              ["sandwich" nil] #{},
              ["beach" "and"] #{"their"},
              ["went" "to"] #{"eat"},
              ["the" "potato"] #{"went"},
              ["to" "the"] #{"beach"},
              ["potato" "went"] #{"off" "to"},
              ["unicorn" "and"] #{"the"},
              ["brother" "the"] #{"potato"},
              ["The" "unicorn"] #{"and"},
              ["went" "off"] #{"to"},
              ["a" "sandwich"] #{}} (word-seq ex))))))

(deftest test-part->word-seq
 (testing "String with spaces and new lines"
   (let [example "The unicorn and the potato went\noff to the beach and their brother the potato appeared"]
   (is (= {["and" "the"] #{"potato"},
           ["and" "their"] #{"brother"},
           ["potato" "appeared"] #{},
           ["off" "to"] #{"the"},
           ["their" "brother"] #{"the"},
           ["went" "off"] #{"to"},
           ["the" "beach"] #{"and"},
           ["beach" "and"] #{"their"},
           ["appeared" nil] #{},
           ["the" "potato"] #{"went" "appeared"},
           ["to" "the"] #{"beach"},
           ["potato" "went"] #{"off"},
           ["unicorn" "and"] #{"the"},
           ["brother" "the"] #{"potato"},
           ["The" "unicorn"] #{"and"}}
          (part->word-seq example))))))

(deftest test-walk-seq
  (let [chain {["and" "the"] #{"potato"},
               ["and" "their"] #{"brother"},
               ["potato" "appeared"] #{},
               ["off" "to"] #{"the"},
               ["their" "brother"] #{"the"},
               ["went" "off"] #{"to"},
               ["the" "beach"] #{"and"},
               ["beach" "and"] #{"their"},
               ["appeared" nil] #{},
               ["the" "potato"] #{"went" "appeared"},
               ["to" "the"] #{"beach"},
               ["potato" "went"] #{"off"},
               ["unicorn" "and"] #{"the"},
               ["brother" "the"] #{"potato"},
               ["The" "unicorn"] #{"and"}
               ["the" "unicorn"] #{"hello"},
               ["said" "the"] #{"unicorn"},
               ["hello" "said"] #{ "the"}}]
    (testing "dead end"
      (let [prefix ["the" "potato"]]
        (is (= ["the" "potato" "appeared"]
               (walk-seq prefix chain prefix)))))
    (testing "multiple choices"
      (with-redefs [shuffle (fn [c] c)]
        (let [prefix ["the" "potato"]]
          (is (= ["to" "the" "potato" "appeared"]
                 (walk-seq prefix chain prefix))))))
    (testing "repeating chains"
      (with-redefs [shuffle (fn [c] (reverse c))]
        (let [prefix ["And" "the"]]
          (is (> 140
                 (count (apply str (walk-seq prefix chain prefix)))))
          (is (= ["hello" "said" "the" "unicorn" "hello" "said" "the" "unicorn"]
                 (take 8 (walk-seq prefix chain prefix)))))))))




(deftest test-generate-text
  (with-redefs [shuffle (fn [c] c)]
    (let [chain {["who" nil] #{}
                 ["Pobble" "who"] #{}
                 ["the" "Pobble"] #{"who"}
                 ["Grouse" "And"] #{"the"}
                 ["Golden" "Grouse"] #{"And"}
                 ["the" "Golden"] #{"Grouse"}
                 ["And" "the"] #{"Pobble" "Golden"}}]
      (is (= "the Pobble who" (generate-text "the Pobble" chain)))
      (is (= "And the Pobble who" (generate-text "And the" chain))))))

(deftest test-end-at-last-punctuation
  (testing "Ends at the last punctuation"
    (is (= "Bye for now."
           (end-at-last-punctuation "Bye for now. So that we")))
    (testing "Replaces ending comma with a period"
      (is (= "Bye for now."
             (end-at-last-punctuation "Bye for now, So that"))))
    (testing "If there are no previous punctuations, just leave it alone and add one at the end"
      (is ( = "Bye for now I said."
              (end-at-last-punctuation  "Bye for now I said "))))
    (testing "works with multiple punctuation"
      (is ( = "The potato climbed over a mountain. He was out for blood."
              (end-at-last-punctuation  "The potato climbed over a mountain. He was out for blood.He needed"))))))
