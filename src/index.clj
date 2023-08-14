(ns index
  {:nextjournal.clerk/visibility {:code :hide}
   :nextjournal.clerk/open-graph {:image "https://cdn.nextjournal.com/data/QmPyuhrmAXxaTv7txqwMAHnJ3ALgx6iDeKtQFEQocRbB1i?filename=nj-blog-og-image.png&content-type=image/png"}}
  (:require [nextjournal.clerk :as clerk]
            [journal.index :as idx]))

(clerk/html [:style "h1 { margin-bottom: 0; font-size: 2.6em !important;}
h1 + p { font-size: 1.7em; position: relative; padding-bottom: 40px;}
h1 + p:after { content: \"\"; position: absolute; left: 0; bottom: 0; border-bottom: 2px solid #ccc; width: 350px;"])

;; # Welcome to the Journal of my engineering adventures
;; Follow along with what I'm working on, and the topics I'm passionate about.

^{::clerk/viewer clerk/html :nextjournal.clerk/visibility {:result :show}}
(into [:<>]
      (map idx/link-article)
      ['articles/vespa-lucene-linguistics-analyzers
       'articles/vespa-lucene-linguistics
       'articles/vespa-searcher-properties
       'articles/vespa-ip-vs-hostname])
