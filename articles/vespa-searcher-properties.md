```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(ns vespa-searcher-properties
  {:nextjournal.clerk/toc true}
  (:require
    [clojure.string :as str]
    [clojure.data.json :as json]
    [journal.utils.jsoup :as jsoup]
    [nextjournal.clerk :as clerk]
    [nextjournal.clerk.viewer :as v]
    [journal.vespa.searcher.core :as searcher]))

{:nextjournal.clerk/visibility {:code :hide :result :show}}
```
# Exploring Vespa Searchers

2023-07-11

## TL;DR

[Vespa](https://vespa.ai) [`Searchers`](https://docs.vespa.ai/en/searcher-development.html) can handle almost arbitrary HTTP requests with only a couple of gotchas.

## Context

Say you have an existing search API [service](https://microservices.io) that under the hood calls some search engine[^search-engine].
The search API receives a query and all it's context (filters, flags, etc.) with an HTTP request.
And one day you were given a task to [AB test](https://en.wikipedia.org/wiki/A/B_testing) Vespa as an alternative search engine.

A sub-tasks is to set up the query construction.
One approach could be to construct a [YQL](https://docs.vespa.ai/en/query-language.html)[^yql] inside your search API and then to make an HTTP call to Vespa.
An alternative approach could be to implement the query construction logic **inside** a Vespa searcher by passing the HTTP request of the search API service directly to Vespa.
Which one to choose?

Vespa [documentation](https://docs.vespa.ai/en/query-api.html#http)[^vespa-docs] says:

[^vespa-docs]: Documentation is already excellent!

> Best practise [^http-best-practices] for queries is submitting the user-generated query as-is to Vespa, then use Searcher components to implement additional logic.

So, in this article we'll explore the Vespa searchers and their handling of search query data coming via an HTTP request.

[^yql]: Yahoo! Query Language
[^search-engine]: such as [Elasticsearch](https://www.elastic.co/what-is/elasticsearch#)

## Vespa Searchers

When in the [`services.xml`](https://docs.vespa.ai/en/reference/services.html) of your Vespa
[application package](https://docs.vespa.ai/en/application-packages.html) under the
[`<container>`](https://docs.vespa.ai/en/reference/services-container.html) you include a
[`<search/>`](https://docs.vespa.ai/en/reference/services-search.html) tag, you specify the search part of the
[container](https://docs.vespa.ai/en/operations/container.html) configuration.
Part of which is the _[chain](https://docs.vespa.ai/en/components/chained-components.html) of searchers_.
When deployed a chain of searchers is responsible for turning a
[`Query`](https://github.com/vespa-engine/vespa/blob/master/container-search/src/main/java/com/yahoo/search/Query.java#L88)
into the [`Result`](https://github.com/vespa-engine/vespa/blob/master/container-search/src/main/java/com/yahoo/search/Result.java).

The simplest possible, the noop searcher looks like this:
```c++  
public Result search(Query query, Execution execution) {
    return execution.search(query);
}
```

## Demo Vespa Application Package

I learn best by building things.
To help myself learn how Vespa searchers work I've created [^created] a tiny Vespa app.
The source code is [here](https://github.com/dainiusjocas/journal/tree/main/src/journal/vespa/searcher/vap).
All examples used in this articles are tested to be working with this setup.

[^created]: To be honest, I've started with a demo [`album-recommendation-java`](https://github.com/vespa-engine/sample-apps/blob/master/album-recommendation-java/) app and trimmed it

## HTTP Requests

HTTP request[^rich-hickey] carries data in the:
- Body
- [Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers)
- [URL query string](https://en.wikipedia.org/wiki/Query_string)

Let's inspect how data in [HTTP requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages) is represented in a Vespa Searcher.

### Body

Your JSONs are there.
In case your request body is set of key-value pairs, then the data is parsed into
[`Properties`](https://github.com/vespa-engine/vespa/blob/master/container-search/src/main/java/com/yahoo/search/query/Properties.java)[^properties].
You can access properties like this:
```c++
query.properties().get("propertyKey")
```

[^properties]: I think of `Properties` as a hashmap with several additional getters that does the conversion to a primitive type,
e.g. `getDouble`.

The [Query API](https://docs.vespa.ai/en/reference/query-api-reference.html) has a lot of standard parameters.
An interesting bit is that some parameters have aliases, e.g. [`model.queryString`](https://docs.vespa.ai/en/reference/query-api-reference.html#model.querystring) alias is `query`.
Even more interesting thing is that aliases are case-insensitive!
IMO, that really shows that Vespa was in development for a long [time](https://www.tumblr.com/yahoodevelopers/165763519943/open-sourcing-vespa-yahoos-big-data-processing) (since 2004?).

If your data is nested then the handling is a bit more involved.
However, Vespa has you covered: [query profiles](https://docs.vespa.ai/en/query-profiles.html).
Query profiles can specify:
- values that are injected into the `Query`;
- defaults;
- type checking of nested data structures;
- and other neat tricks.

#### Curious case of nested JSON objects

One gotcha of the Searcher is that nested JSON objects are flattened!
In the Query API [docs](https://docs.vespa.ai/en/reference/query-api-reference.html) we can see that the property names are like this [`model.queryString`](https://docs.vespa.ai/en/reference/query-api-reference.html#model.querystring).
When doing the HTTP request with the JSON body you can send a value with the flat-dot notation:

```clojure
(clerk/html 
  [:pre (searcher/resp-to-curl (searcher/query-flat-dot))])
```

Or you can send a nested JSON-structure.

```clojure
(clerk/html
  [:pre (searcher/resp-to-curl (searcher/query-nested-json))])
```

These two are the same from the Vespa point of view.
This behaviour is [documented](https://docs.vespa.ai/en/query-api.html#using-post).

NOTE: in case your search API HTTP request contain properties that clash with the Vespa Query API,
then I'd suggest to add a searcher that is executed early in the **chain** to handle the problematic keys somehow.

### HTTP Headers

HTTP headers are represented as a `Map<String, List<String>>`.

In the searcher you can access headers like this:

```c++
query.getHttpRequest().getJDiscRequest().headers()
```

### URL Query String

URL query string is parsed into a `Map<String, String>`.
You can access them in your searcher like this:

```c++
query.getHttpRequest().propertyMap()
```

Also, URL query string parameters are added into the `properties`!

NOTE: URL query parameters have a **precedence** over parameters specified in the HTTP request body.

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}
  :nextjournal.clerk/no-cache true}
(def response (searcher/http-call
            {"foo-priority-param" "from-query-string"}
            {}
            {"foo-priority-param" "from-body"}))

{:nextjournal.clerk/width :wide}
^:nextjournal.clerk/no-cache
(clerk/html [:pre (searcher/resp-to-curl response)])

;; To which Vespa responds:

{:nextjournal.clerk/width :full}
^:nextjournal.clerk/no-cache
(clerk/html [:pre (searcher/http-resp-pprint response)])
```

NOTE: passing search parameters via query string (e.g. YQL with an entire embedding) has a significant performance penalty.

## Anything interesting about HTTP responses?

It is possible to set custom [HTTP response headers](https://developer.mozilla.org/en-US/docs/Glossary/Response_header).
In your searcher:

```c++
result.getHeaders(true).put("X-Foo-Header", "foo-value");
```

NOTE: In order to enable custom HTTP response headers don't forget to set

```clojure
{:nextjournal.clerk/width :wide}
(clerk/html 
  [:pre
   (map str (jsoup/select
              (jsoup/parse
                (slurp "src/journal/vespa/searcher/vap/pom.xml"))
              "project > build > plugins > plugin > configuration > failOnWarnings"))])
```

for the Vespa `bundle-plugin` in the `pom.xml`, [source](https://github.com/dainiusjocas/journal/tree/main/src/journal/vespa/searcher/vap/pom.xml).

## Experimentation

The `EchoSearcher` source code:

```clojure
{:nextjournal.clerk/width :wide}
^:nextjournal.clerk/no-cache
(clerk/code
  {::clerk/opts {:language "java"}}
  (str/join "\n" (->> (slurp "src/journal/vespa/searcher/vap/src/main/java/lt/jocas/vespa/searcher/EchoSearcher.java")
                      (str/split-lines)
                      (drop 9))))
```

The Searcher echoes the params from the request that starts with `foo`[^why-foo] and adds fields for URL query strings and HTTP headers.

[^why-foo]: Because Vespa by default adds many more properties.

All-in-one request example:

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def resp (searcher/http-call))

{:nextjournal.clerk/width :full}
^:nextjournal.clerk/no-cache
(clerk/html [:pre (searcher/resp-to-curl resp)])

;; To which Vespa responds:

{:nextjournal.clerk/width :full}
^:nextjournal.clerk/no-cache
(clerk/html [:pre (searcher/http-resp-pprint resp)])
```

In the `body` we have one hit that has fields:
- `HTTP_HEADERS`;
- `URL_QUERY_STRING`;
- multiple fields starting with `foo`: all values are serialized into strings;

Also, in the response we can see our custom HTTP header.

## Conclusions

Vespa searcher architecture is flexible enough to (re)implement your custom search API.
Searchers handle HTTP requests with any incoming data.
Next we'll explore Searcher chains.

[^http-best-practices]: Other best practices can be found [here](https://cloud.vespa.ai/en/http-best-practices)
[^rich-hickey]: [Rich Hickey](https://en.wikipedia.org/wiki/Rich_Hickey) HTTP request rant [video](https://www.youtube.com/watch?v=aSEQfqNYNAc)
