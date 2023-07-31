# Vespa Lucene Linguistics Analyzer Priority

#### 2023-07-28

## TL;DR

The analysis priority is as follows:
```text
Linguistics configuration > Components > Language Defaults > StandardAnalyzer
```

## Context

When it comes to Analyzers the Vespa Lucene Linguistics component is highly configurable.
However, the configuration with XML is a bit verbose and your IDE can't help preventing mistakes.
There is another way to specify Lucene analyzers: add Analyzer classes into the classpath and
declare them as components in your `services.xml`.
When Vespa container is being started up JDisc creates a [ComponentRegistry<Analyzer>](https://docs.vespa.ai/en/jdisc/injecting-components.html#depending-on-all-components-of-a-specific-type)
and injects it into LuceneLinguistics.
Exposing this option allows you to craft analyzers with the help of your favourite IDE.

## Analyzer components

The resource files, e.g. stopword dictionaries should be loaded as resources from the Java project and not from the application package.

### When there is only a no argument constructor

### When there are multiple constructors.

## Default Analyzers

`LuceneLinguistics` already depends on `lucene-analysis-common` package which includes many analyzers for different languages.
Why not to use them?
The `DefaultAnalyzers` class does just that.
It stores a mapping from the `Language` to an `Analyzer` object.
It should serve as a solid base for implementing lexical search functionality.

## Priority of analyzers

Since there are multiple ways to define an analyzer for a language there might be conflicts.
LuceneLinguistics defines the following conflict resolution strategy:

````text
if Analyzer is already known
- then return it
else if there is a configured component
- then setup, add to language map, and return it
else if there is a configured analyzer component
- then add it to language map, return it
else if there is a default analyzer
- then add to language map and return it
else return the standard analyzer
````

## A note on schema

Lucene Linguistics ignores the stemming settings at both: index and query time.
To prevent unnecessary work you should disable stemming so that even the noop stemmer is applied.

## Conclusion

`LuceneLinguistics` implementation turned out to be highly flexible.
It supports multiple extension mechanisms which.
