# Clojurist Together Update 2

#### 2023-08-31

The highlight of this update is the release of the [lucene-monitor](https://github.com/dainiusjocas/lucene-monitor) Clojure library.

`lucene-monitor` is a Clojure wrapper around the [Apache Lucene Monitor framework](https://lucene.apache.org/core/9_7_0/monitor/org/apache/lucene/monitor/package-summary.html).
I've tried to make the library simple to get started and flexible **when** needed.
`lucene-monitor` provides a pretty tasty data-driven API
and, given my recent interest in Clojure transducers, a transducer compatible API.

Check it out and let me know what you think!

## Updates

As of now, 4 Clojure libraries are extracted out of the `lucene-grep`:
- [lucene-monitor](https://github.com/dainiusjocas/lucene-monitor): a wrapper for the Lucene Monitor framework;
- [lucene-custom-analyzer](https://github.com/dainiusjocas/lucene-custom-analyzer): data-driven builder for Lucene Analyzers;
- [lucene-query-parsing](https://github.com/dainiusjocas/lucene-query-parsing): data-driven builder of Lucene Query Parsers;
- [lucene-text-analysis](https://github.com/dainiusjocas/lucene-text-analysis): helpers to experiment with the Lucene Analyzers.

All these libraries were updated to the newest Lucene version.

## Other things I've worked on

### Lucene Grep

The `lucene-grep` was updated and released with these improvements:
- Lucene 9.7.0;
- The prepared Lucene Analyzers can now be added and loaded via SPI.

Also, I've experimented with [Apache OpenNLP integration](https://github.com/dainiusjocas/lucene-grep/pull/219) into the `lucene-grep`.
So far I don't know whether to include new dependencies by default, because it makes the binary bigger, compile times longer,
and the `lucene-opennlp` includes a pretty outdated version of the OpenNLP library.

### Lucene in Vespa.ai

I've [contributed a Lucene Linguistics](https://github.com/vespa-engine/vespa/pull/27929) component to [Vespa.ai](https://vespa.ai/).
This is not strictly based on `lucene-grep` or related to Clojure 
(maybe it is possible to start a REPL in Vespa container nodes after all?)
but the work is heavily inspired by [lucene-custom-analyzer](https://github.com/dainiusjocas/lucene-custom-analyzer) library
(and learnings while making it) which was extracted from the `lucene-grep`.
Sample apps with the Lucene Linguistics component are coming [hopefully soon](https://github.com/vespa-engine/sample-apps/pull/1264).
The component should make the transition from Lucene based search engines like Elasticsearch to Vespa almost a mechanical task.
I encourage anyone interested in search, recommendation systems, or information retrieval in general, to give Vespa.ai a try.
it is great!

## What is next?

Even though the sponsorship from Clojurist Together is over I'm planning to have more fun with Clojure and Lucene: 

- I'll try to integrate the extracted libraries to other Clojure projects that depend on Lucene.
- Build a demo of an [Elasticsearch-Percolator-like](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-percolate-query.html) 
system that does monitoring in a scalable and distributed mode.
- A blog post on using the shiny new [Word2VecSynonymFilter](https://lucene.apache.org/core/9_7_0/analysis/common/org/apache/lucene/analysis/synonym/word2vec/Word2VecSynonymFilter.html) from Clojure.

---

P.S. A huge shout-out for [Clojurist Together](https://www.clojuriststogether.org) for sponsoring my open source work!
