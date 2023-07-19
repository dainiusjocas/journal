(ns journal.vespa.linguistics.core)


(comment
  (-> (http-call {"language"          "fr"}
                 {}
                 {"model.queryString" "dogs cats rainbows"
                  "yql"               "select * from sources * where ({stem: true}userInput(@model.queryString))"
                  "tracelevel"        5})
      :body
      json/read-str))
;; docker exec vespa bash -c '/opt/vespa/bin/vespa-proton-cmd --local triggerFlush && \
;    /opt/vespa/bin/vespa-index-inspect dumpwords \
;    --indexdir /opt/vespa/var/db/vespa/search/cluster.music/n0/documents/music/0.ready/index/index.fusion.3 \
;    --field album'
