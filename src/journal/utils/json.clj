(ns journal.utils.json
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [charred.api :as c]))

(defn pprint [obj]
  ;; I don't like the pretty print behaviour
  (str/replace
    (c/write-json-str obj {:indent-str "  "})
    #"(?s)(:\s+\{)"
    ": {"))

(defn read-str [string] (json/read-str string))

(defn json-str [object] (json/json-str object))
