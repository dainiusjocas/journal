```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(ns simple-slideshow
  (:require
    [nextjournal.clerk :as clerk]
    [nextjournal.clerk-slideshow :as slideshow]
    [nextjournal.clerk.viewer :as v])
  (:import (java.time Instant)))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(clerk/add-viewers! [slideshow/viewer])
```

# Vespa Intro

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}
  :nextjournal.clerk/no-cache   true}
(clerk/html
  [:h3 {:style {:color :red}}
   "If they can't find it they cant buy it."])
^{:nextjournal.clerk/visibility {:code :hide :result :show}
  :nextjournal.clerk/no-cache   true}
(clerk/html [:ul
             [:li [:h4 "Dainius Jocas"]]
             [:li [:h4 (.toString (Instant/now))]]])
```

---

## whoami

```edn
{:name          "Dainius Jocas"
 :company       {:name    "Vinted"
                 :mission "Make second-hand the first choice worldwide"}
 :role          "Search Engineer"
 :website       "https://www.jocas.lt"
 :twitter       "@dainius_jocas"
 :github        "dainiusjocas"
 :author_of_oss ["lucene-grep" "quarkus-lucene" "clj-jq" "ket" "esql"]}
```

---

## Agenda

- What is Vespa?
- History
- Architecture
- What it is good for?
- Compare: Vespa vs. Elasticsearch
- Who uses Vespa?
- Questions
- Resources

---

## What is Vespa?

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}}
(clerk/image "https://github.com/dainiusjocas/journal/blob/main/articles/vespa-intro/logo.png?raw=true")
```

- `Big Data + AI, online.`
- The Open big data serving Engine
- Store, search, rank, organize at user serving time
- VeSPA - Vertical Search Platform Architecture
- Open Source, Apache 2.0 License
- Developed by Yahoo!

---

## History

- alltheweb.com
- In development from ~2004 (~20 years!)
- Comes from Norway, Trondheim
- Cousin of Hadoop
  - Hadoop: Offline computation
  - Vespa: Online serving
  - Both come from Yahoo
  - Both for WEB search problem
- Open sourced in 2017
- New releases 2x per week

---

## Vespa Architecture

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}}
(clerk/image "https://github.com/dainiusjocas/journal/blob/main/articles/vespa-intro/vespa-overview.png?raw=true")
```

--- 

## What is it good for?

- Search
  - Lexical search (like Elasticsearch)
  - Approximate Nearest Neighbours (ANN, like FAISS)
  - filtering, sorting, grouping on metadata
  - Hybrid! (i.e. sparse + dense, e.g. keywords + text embeddings)
  - Multimodal search (e.g. images + text)
- Personalization / recommendation / targeting 
- Machine Learning (ML) model inference
  - TensorFlow, PyTorch, and ONNX runtime
  - GPU support!
- Vector Database (CRUD!)

---

[//]: # (## Note on Hybrid search)

[//]: # ()
[//]: # (- Keyword search can get the job done)

[//]: # (- Adding hybrid can help with)

[//]: # (  - When not enough results are retrieved)

[//]: # ()
[//]: # (---)

## Vespa vs. Elasticsearch

- Multiple threads per search
- Highly available
- Random writes with immediate availability for search
- Efficient partial updates, e.g. storing counters makes sense
- Automatic data distribution, **not** shard based
- Schema is strict
- Easy to extend default functionality
- Text analysis setup is "unique"

---

## Who uses Vespa?

- Yahoo!: 200+ apps: Ads, Mail, News, TechCrunch, Flickr, etc.
- Spotify: podcast search
- OkCupid: love :)
- otto.de: search suggestions
- Qwant.com: France centric web search without tracking
- **Vinted: item recommendations!**
- Vespa Cloud

---

## Usage

- Start on your laptop with a Docker :)
- Deploy a bunch of Vespa Docker containers in your infrastructure
  - Several for `configserver` nodes
  - Sufficient number of `services` nodes for your data and requests
  - Don't touch the machines ever again except for version upgrades
- Deploy your Vespa Application Package (VAP)
- Feed/Index data with REST+JSON
- Query with YQL
- Metrics in Prometheus format are exposed out of the box!
- Go crazy with custom functionality in the VAP :)
  - Processing chains (similar to interactors) for both indexing and searching
  - Add Lucene analyzers if that's your cup of tea
- Vespa Cloud is an option

---

## Feeding data

- HTTP+REST+JSON
```shell
$ curl -s -X POST -H "Content-Type: application/json" http://localhost:8080/document/v1/music/music/docid/123 -d'
{
    "fields": {
        "title": "Best of Bob Dylan"
    }
}'
```
- `vespa-feed-client`: Java library and CLI client
- vespa-cli
```shell
$ vespa feed src/main/application/ext/document.json
```
---

## Querying:

- YQL: Yahoo Query Language
- Similar to SQL
- Examples:

```text
select * from doc where true
select * from doc where term_count = 1083
select * from doc where last_updated > 1646167144
select * from doc where title contains "ranking"
select * from doc where default contains phrase("question","answering")
select * from doc where artist contains ({stem: true}"dogs cats")
select * from doc where true limit 0 | all( group( fixedwidth(term_count,100) ) each( output( avg(term_count) ) ) )
```
- Can handle almost arbitrary JSONs with a bit of extra work

---

## Ranking

- Ranking is a mathematical expression
- Proper architecture for tuning the "relevance vs. computation cost" tradeoff

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :show}
  :nextjournal.clerk/width :full}
(clerk/image "https://github.com/dainiusjocas/journal/blob/main/articles/vespa-intro/phased-ranking.png?raw=true")
```

---

## Questions?

---

## Resources

- https://vespa.ai
- https://blog.vespa.ai
- https://docs.vespa.ai
- https://slack.vespa.ai/
- https://github.com/vespa-engine/vespa
- https://github.com/vespa-engine/sample-apps
- https://github.com/vespa-cloud
