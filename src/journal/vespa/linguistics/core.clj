(ns journal.vespa.linguistics.core
  (:require [journal.utils.json :as json]
            [clj-test-containers.core :as tc]
            [clojure.string :as str]
            [journal.vespa.searcher.core :as searcher]
            [journal.vespa.linguistics.container :as container]))

(def field "mytext")

(defn feed-document! [in-container]
  (tc/execute-command!
    in-container
    ["sh" "-c" (format "(cd %s && vespa feed src/main/application/ext/document.json)"
                       container/vap-dir)]))

(defn delete-document! [in-container]
  (tc/execute-command!
    in-container
    ["sh" "-c" (format "vespa document remove id:mynamespace:lucene::mydocid")]))

(defn list-index-files [container]
  (-> (tc/execute-command!
        container
        ["sh" "-c" "ls /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/"])
      (:stdout)
      (str/trim)))

(defn flush-to-disk! []
  (tc/execute-command!
    @container/vespa
    ["sh" "-c" "/opt/vespa/bin/vespa-proton-cmd --local triggerFlush"]))

(defn list-tokens []
  (let [index-dir (list-index-files @container/vespa)]
    (->> (tc/execute-command!
           @container/vespa
           ["sh" "-c"
            (format
              "/opt/vespa/bin/vespa-index-inspect dumpwords \\
          --indexdir  /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/%s/ \\
          --wordnum \\
          --field %s"
              index-dir field)])
         (:stdout))))

(defn show-postings []
  (let [index-dir (list-index-files @container/vespa)]
    (->> (tc/execute-command!
           @container/vespa
           ["sh" "-c"
            (format
              "/opt/vespa/bin/vespa-index-inspect showpostings \\
          --indexdir  /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/%s/ \\
          --field %s --transpose"
              index-dir field)])
         (:stdout))))

(comment
  (do
    (container/build-vap-and-deploy! @container/vespa)
    (Thread/sleep 1000)
    (delete-document! @container/vespa)
    (feed-document! @container/vespa)
    (flush-to-disk!)
    (println "TOKEN LIST:\n" (list-tokens))
    (println "POSTINGS:\n" (show-postings))))

(defn vap-container-port [] (get (:mapped-ports @container/vespa) 8080))

(comment
  (in-ns 'journal.vespa.linguistics.core)
  @container/vespa
  (tc/start! @container/vespa)

  (container/build-vap-and-deploy! @container/vespa)

  (feed-document! @container/vespa)

  (tc/stop! @container/vespa)

  (-> (searcher/http-call
        {}
        {}
        {"query"          "havneenheter"
         "grammar"        "any"
         "model.language" "no"
         "yql"            "select * from sources * where userInput(@query) timeout 100"
         "traceLevel"     "2"}
        (vap-container-port))
      :body
      (json/read-str))

  (-> (searcher/http-call
        {}
        {}
        {"query"          "jedeno i dwa"
         "grammar"        "any"
         "model.language" "pl"
         "yql"            "select * from sources * where userInput(@query) timeout 100"
         "traceLevel"     "2"}
        (vap-container-port))
      :body
      (json/read-str))

  (-> (searcher/http-call
        {}
        {}
        {"query"          "somethings"
         "grammar"        "any"
         "model.language" "en"
         "yql"            "select * from sources * where userInput(@query) timeout 100"
         "traceLevel"     "2"}
        (vap-container-port))
      :body
      (json/read-str))

  (-> (searcher/http-call
        {}
        {}
        {"query"          "somethings"
         "grammar"        "any"
         "model.language" "en"
         "yql"            "select * from sources * where true timeout 100"
         "traceLevel"     "2"}
        (vap-container-port))
      :body
      (json/read-str)))
