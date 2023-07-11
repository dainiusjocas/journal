(ns journal.utils.jsoup
  (:require [clojure.string :as s])
  (:import (java.io StringReader StringWriter)
           (java.util Iterator)
           (javax.xml.transform OutputKeys TransformerFactory)
           (javax.xml.transform.stream StreamResult StreamSource)
           (org.jsoup Jsoup)
           (org.jsoup.nodes Attribute Attributes Document Element)
           (org.jsoup.parser Parser)
           (org.jsoup.select Elements)))

(set! *warn-on-reflection* true)
(defn parse ^Document [^String xml-string]
  (Jsoup/parse xml-string "" (Parser/xmlParser)))

(defn select ^Elements [^Document doc ^String selector]
  (.select doc selector))

(defn get-text ^String [^Element e] (.text e))

(defn select-text
  "Returns a list of strings of selected nodes."
  [^Document doc ^String selector]
  (mapv get-text (select doc selector)))

(defn select-attrs
  "Returns a list of node attribute maps matched by selector"
  [^Document doc ^String selector]
  (sequence
    (comp
      (map (fn [^Element e] (.attributes e)))
      (map (fn attr-map [^Attributes a]
             (let [^Iterator i (.iterator a)]
               (reduce (fn [acc ^Attribute val]
                         (assoc acc (.getKey val) (.getValue val)))
                       {}
                       (iterator-seq i))))))
    (select doc selector)))

(comment
  (select-text
    (parse "<div><foo>qq</foo><foo>ww</foo></div>")
    "div foo")
  (select
    (parse "<div><foo a=\"a\"></foo><foo a=\"b\"></foo></div>")
    "div foo")
  (select-attrs
    (parse "<div><foo a=\"a\"></foo><foo a=\"b\"></foo></div>")
    "div foo"))

(defn apply!
  "Applies a sequence of transformations on a Document.
  Returns a modified document.
  Default :op is to 'replace'.
  Attributes are only to be set, no removal,
  when attrs {:key nil}, then overrides value to empty string."
  [^Document doc xforms]
  (doseq [{:keys [selector val op attrs]} (remove nil? xforms)]
    (assert (string? selector)
            (format "Selector must be a string! Was '%s'" selector))
    (let [val (str val)
          op (keyword op)]
      (when-not (and (s/blank? val) (empty? attrs) (not= :delete op))
        (let [elems (select doc selector)]
          (when (or (not (s/blank? val)) (= :delete op))
            (case op
              :replace (.html elems val)
              :append (.append elems val)
              :prepend (.prepend elems val)
              :delete (.remove elems)
              (.html elems val)))
          (doseq [[k v] attrs] (.attr elems (str (name k)) (str v)))))))
  doc)

(comment
  (apply!
    (parse "<div><foo></foo></div>")
    [{:selector "div foo"
      :val      "<bar>12</bar>"
      :op       "replace"
      :attrs    {:key "value"}}])
  (apply!
    (parse "<div><foo></foo></div>")
    [{:selector "div foo"
      :val      "<bar>12</bar>"
      :attrs    {:key "value"}}])
  (apply!
    (parse "<div><foo></foo></div>")
    [{:selector "div foo"
      :val      "<bar>12</bar>"}])
  (apply!
    (parse "<div><foo></foo></div>")
    [{:selector "div foo"
      :attrs    {:key "value"}}])
  (apply!
    (parse "<div><foo></foo></div>")
    [{:selector "div foo"}])

  (apply!
    (parse "<div><foo></foo></div>")
    [{:selector "div foo"
      :val      5}])
  (apply!
    (parse "<div><foo></foo></div>")
    [{:selector 5
      :val      5}])

  [{:selector ""
    :html     ""
    :op       "prepend|append|replace"
    :attrs    {:key "value"}}])

(defn ppxml [xml]
  (let [in (StreamSource. (StringReader. xml))
        writer (StringWriter.)
        out (StreamResult. writer)
        transformer (.newTransformer (TransformerFactory/newInstance))]
    (.setOutputProperty transformer OutputKeys/INDENT "yes")
    (.setOutputProperty transformer "{http://xml.apache.org/xslt}indent-amount" "2")
    (.setOutputProperty transformer OutputKeys/METHOD "xml")
    (.setOutputProperty transformer OutputKeys/STANDALONE "yes")
    (.transform transformer in out)
    (-> out .getWriter .toString (.replaceAll "\\n\\s*\\n" "\n"))))

(defn pretty-print ^String [^Document document]
  (ppxml (.outerHtml document)))
