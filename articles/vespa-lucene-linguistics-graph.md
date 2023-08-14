# (DRAFT) LuceneLinguistics and TokenStream Graph

## TL;DR

TODO: 

## Context

`LuceneLinguistics` enables to use Lucene to analyze text in your Vespa applications.
All great, but [Lucene TokenStreams are actually graphs](https://blog.mikemccandless.com/2012/04/lucenes-tokenstreams-are-actually.html).
How does LuceneLinguistics package handles it?

## Graphs?

What does it mean a token stream is a graph?
First, a graph is a bunch of nodes connected with edges.

TODO: example image.

So, Lucene TokenStream can be visualized as a graph of Tokens where nodes are position numbers,
and edges are actual token string representations.

TODO: example image of a graph as taken from the mccandles post draw with clerk and lucene custom analysis.

## Vespa Linguistics

Vespa has no problems to add multiple tokens in the same position.
This means, that you can use your favourite `SynonymGraphTokenFilter` as you did in Elasticsearch.

One complication is it is not advisable to use synonym graph at index time.
So, you must come up with creating ways how to make text anlysis asymetric in Vespa.

Let's look at how token graphs are represented both at index and search time.

### Example at index-time

TODO: 
  - setup english analyzer
  - Index document
  - get posting list for that document

### Example in search-time

TODO: 
  - issue a query examples and observe the YQL.
  - Show what is found and what is not.

### Vespa Query Rewriting

Yeah, synonyms are not the ultimate solution to solve the search recall problems.
There are other techniques for query rewriting, e.g.:
- Rule based engines, e.g. [Querqy](https://github.com/querqy/querqy/)
  - https://opensourceconnections.com/blog/2021/10/19/fundamentals-of-query-rewriting-part-1-introduction-to-query-expansion/
- Vespa semantic query rewriting
  - https://docs.vespa.ai/en/query-rewriting.html
  - https://docs.vespa.ai/en/reference/semantic-rules.html
- Neural retrieval model based approaches
  - [SPLADE](https://github.com/naver/splade)
  - [ELSER](https://www.elastic.co/guide/en/machine-learning/current/ml-nlp-elser.html)
