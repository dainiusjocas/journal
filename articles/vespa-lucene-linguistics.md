# Vespa Lucene Linguistics Implementation

### 2023-07-27

## TL;DR

Introducing Lucene based Vespa Linguistics package!
It makes migrating from Lucene based search engines to Vespa significantly easier.

## Context

Say you are migrating from Elasticsearch to Vespa.
Over the years you've crafted custom Elasticsearch analyzers for multiple languages with dictionaries for synonyms, stopwords, etc.
Wouldn't it be great if you could just **migrate** those analyzers without completely **rewriting** (and testing) them on Vespa?

Vespa has a concept of `Linguistics` component. 
It plays a similar role to what analyzers do in Elasticsearch.
So, implementing a Linguistics component based on Lucene would allow **migrating** analyzers to Vespa.

I've rolled out my sleeves and worked out a POC of the Vespa `LuceneLinguistics` to check if that would work.
This article describes my adventures and the current state.

## Design

### Configuration



## Usage

The package is not properly released yet.
But it is coming soon.
For now you have to get the code from (here)[] and depending on your usage build it manually.

### As a component

If your application package is simple enough that you can just `vespa deploy` and
you don't have any custom Java components 
it is possible to add custom Vespa components directly into you application package's `components` directory.
To do that:

```shell
# Build the LuceneLinguistics component
mvn clean package
cp target/vespa-lucene-linguistics-1.0.0-deploy.jar $VAP_DIR/components
```

Now you need to add component configuration into `services.xml` file.
NOTE: `bundle` of the component must be `vespa-lucene-linguistics` (the `artifact` in the `pom.xml`).

And you can deploy it:
```shell
cd $VAP_DIR
vespa deploy
```

### As a dependency

In case you are writing your own custom components for the application package,
you must build the code before deploying it.
If you're using maven (for other build tools the story should be similar) then
```shell
mvn clean install
```

Add the `LuceneLinguistics` to your `pom.xml` file:
```xml
<dependency>
  <groupId>lt.jocas</groupId>
  <artifactId>vespa-lucene-linguistics</artifactId>
  <version>1.0.0</version>
</dependency>
```

Now you need to add component configuration into `services.xml` file.
NOTE: `bundle` of the component must be your application package bundle as specified in the `pom.xml`.

And now you can deploy it.
```shell
mvn clean package
vespa deploy
```

## Extensibility

By default `LuceneLinguistics` provide many analysis components for you to use.
In case you need another custom component `LuceneLinguistics` has you covered.
The extensibility assumes that your project contains custom code and has to be built with `maven` for deployment.

### Just add a maven dependency

As described in the design section the components are discovered via SPI.
So, in case you know a library that is readily packaged on Maven Central,
you can just add that dependency to your classpath and it will be available!

An example of such library is the [`stempel`](https://lucene.apache.org/core/8_10_1/analyzers-stempel/overview-summary.html).
It provides an algorithmic stemmer for Polish language.
To use it add a `dependency` in the `pom.xml` like this:
```xml
<dependency>
  <groupId>org.apache.lucene</groupId>
  <artifactId>lucene-analysis-stempel</artifactId>
  <version>9.7.0</version>
  <scope>compile</scope>
</dependency>
```

And once VAP is deployed it is available for usage.

### Implement a custom Lucene analysis component

Leveraging the SPI you can implement a custom Lucene analysis component.
E.g. we want to create a `TokenFilter`.
The plan is:
1. Implement the component,
2. Create a factory class implementation for the (1) that extends `TokenFilterFactory`
3. Register the implementation class for SPI.

See example here.

## Quirks

TODO: indexing, posting lists are not what is expected.

## Future work

- [ ] default analyzers for languages.
- [ ] documentation for the available analysis components?

## Feedback is very welcome!


A Vespa Linguistics component based on Lucene analysis allows you to **migrate** your text analysis

The goal is use Lucene text analysis components in place of Vespa linguistics.

This helps the transition from Lucene based search engines to Vespa.

The inspiration is from:
- [vespa-chinese-linguistics](https://github.com/vespa-engine/sample-apps/blob/master/examples/vespa-chinese-linguistics/src/main/java/com/qihoo/language/JiebaLinguistics.java).
- OpenNlp Linguistics https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/opennlp-linguistics/src/main/java/com/yahoo/language/opennlp/OpenNlpLinguistics.java
- [vespa-kuromoji-linguistics](https://github.com/yahoojapan/vespa-kuromoji-linguistics/tree/main)
- [Clojure library](https://github.com/dainiusjocas/lucene-text-analysis) to work with Lucene analyzers 

The parts of work:
- configuration definition.
- Java resource loader to load resource files.
- SPI based class discovery (HOW WOULD THAT WORK?).
- If no configuration is provided Maybe just use the language analyzers directly?
- Implementation of the Vespa Linguistics interface.
- Support for the additional analyzers:
  - Support for stempel
  - Support for slovak stemmer
  - phonetic analyzer


Galima pabandyti tiesiog grazinti statini sarasa tokenu.
https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/config-model/src/main/java/com/yahoo/vespa/model/search/IndexingDocprocChain.java
https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/docprocs/src/main/java/com/yahoo/docprocs/indexing/IndexingProcessor.java

# Configuration


https://docs.vespa.ai/en/reference/config-files.html

Linguistics config files are distributed by Vespa
```shell
/opt/vespa/var/db/vespa/filedistribution/dd3c228bd80aaf34/lucene
```

Given pom.xml file we can:
```shell
mvn dependency:sources dependency:resolve -Dclassifier=javadoc
```
And then show things from javadoc for a class.
We could construct links to javadocs for google search.

Are the component names really case insensitive?
reverseString vs reversestring


Probably this can be used to inject default analyzers https://docs.vespa.ai/en/jdisc/injecting-components.html#depending-on-all-components-of-a-specific-type

TRY to explain why tokenfilters such as reverseString are bad.
