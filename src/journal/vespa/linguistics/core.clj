(ns journal.vespa.linguistics.core
  (:require [journal.utils.json :as json]
            [clj-test-containers.core :as tc]
            [clojure.string :as str]
            [org.httpkit.client :as http]
            [journal.vespa.searcher.core :as searcher]
            [journal.vespa.linguistics.container :as container]))
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
(comment
  (delete-document! @container/vespa)

  (do
    (feed-document! @container/vespa)
    ; this should be executed first
    (tc/execute-command!
      @container/vespa
      ["sh" "-c" "/opt/vespa/bin/vespa-proton-cmd --local triggerFlush"])

    ; List the tokens
    (let [index-dir (list-index-files @container/vespa)]
      (->> (tc/execute-command!
             @container/vespa
             ["sh" "-c"
              (format
                "/opt/vespa/bin/vespa-index-inspect dumpwords \\
            --indexdir  /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/%s/ \\
            --wordnum \\
            --field mytext"
                index-dir)])
           (:stdout)))))
;(str/split-lines)
;(mapv (fn [kw] (str/split kw #"\t")))
;(into {}),))))


(defn vap-container-port [] (get (:mapped-ports @container/vespa) 8080))

(defn foo []
  @(http/request
     {:method  :get
      :url     (format "http://localhost:%s" (vap-container-port))
      :headers {"Content-Type" "application/json"}}))

(comment
  (in-ns 'journal.vespa.linguistics.core)
  @container/vespa
  (tc/start! @container/vespa)

  (container/build-vap-and-deploy! @container/vespa)

  (feed-document! @container/vespa)

  (tc/stop! @container/vespa)


  (-> (searcher/http-call
        {"language" "en"}
        {}
        {"query"          "ZERO"
         "grammar"        "any"
         "model.language" "en"
         "yql"            "select * from sources * where default contains ({stem: false} \"zero\") timeout 100"
         "traceLevel"     "1"}
        (vap-container-port))
      :body
      (json/read-str)))

;; docker exec vespa bash -c '/opt/vespa/bin/vespa-proton-cmd --local triggerFlush && \
;    /opt/vespa/bin/vespa-index-inspect dumpwords \
;    --indexdir /opt/vespa/var/db/vespa/search/cluster.music/n0/documents/music/0.ready/index/index.fusion.3 \
;    --field album'

; TODO: linguistics reverses term multiple times during query
;
;vespa-index-inspect dumpwords \
;    --indexdir /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/$(ls /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/) \
;    --field mytext

; Check the posting list
; vespa-index-inspect showpostings --indexdir /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/$(ls /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/) --field mytext_index --transpose


; Flush and see the tokens
; /opt/vespa/bin/vespa-proton-cmd --local triggerFlush && vespa-index-inspect dumpwords     --indexdir /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/$(ls /opt/vespa/var/db/vespa/search/cluster.content/n0/documents/lucene/0.ready/index/)     --field mytext_inde
