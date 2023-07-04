(ns journal.index
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.parser :as parser]))

(defn parse [article-id]
  (->> (str article-id ".md")
       (parser/parse-file {:doc? true})))

(defn link-article [article-id]
  (let [{:keys [title]} (parse article-id)]
    [:h2 [:a {:href (clerk/doc-url article-id)} title]]))
