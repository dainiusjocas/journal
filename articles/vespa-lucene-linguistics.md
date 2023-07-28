# Vespa Lucene Linguistics

### 2023-07-27

## TL;DR

Introducing Lucene based Vespa Linguistics component!
It makes migrating from Lucene based search engines to Vespa significantly easier.
The source of component is [here](https://github.com/dainiusjocas/journal/tree/main/src/journal/vespa/linguistics/lucene), the demo application package is [here](https://github.com/dainiusjocas/journal/tree/main/src/journal/vespa/linguistics/vap).

## Context

Say you are migrating from [Elasticsearch](https://www.elastic.co/guide/en/welcome-to-elastic/current/index.html) to [Vespa](https://vespa.ai).
Over the years you've crafted custom Elasticsearch [analyzers](https://www.elastic.co/guide/en/elasticsearch/reference/8.9/specify-analyzer.html) for multiple languages with dictionaries for synonyms, stopwords, etc.
Wouldn't it be great if you could just **migrate** those analyzers without completely **rewriting** (and testing) the logic on Vespa?

Vespa has a concept of a [`Linguistics`](https://docs.vespa.ai/en/linguistics.html) component. 
It plays a similar role to what analyzers do in Elasticsearch.
Elasticsearch's analyzers are [Lucene](https://lucene.apache.org) analyzers.
Lucene is a Java library.
[`Linguistics`](https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/linguistics/src/main/java/com/yahoo/language/Linguistics.java) is a Java interface.
So, implementing a Linguistics component based on Lucene would allow **migrating** Elasticsearch analyzers to Vespa.

I've rolled out my sleeves and worked out a POC of the Vespa [`LuceneLinguistics`](https://github.com/dainiusjocas/journal/blob/main/src/journal/vespa/linguistics/lucene/src/main/java/lt/jocas/vespa/linguistics/LuceneLinguistics.java) to check if the idea is feasible.
This article describes the development adventures and the current state.

## Architecture

In the following subsections technical details are presented.

### Background

After a bit of research I've discovered 4 implementation of the `Linguistics` interface:
- [SimpleLinguistics](https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/linguistics/src/main/java/com/yahoo/language/simple/SimpleLinguistics.java)
- [vespa-chinese-linguistics](https://github.com/vespa-engine/sample-apps/blob/master/examples/vespa-chinese-linguistics/src/main/java/com/qihoo/language/JiebaLinguistics.java)
- [OpenNlpLinguistics](https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/opennlp-linguistics/src/main/java/com/yahoo/language/opennlp/OpenNlpLinguistics.java)
- [vespa-kuromoji-linguistics](https://github.com/yahoojapan/vespa-kuromoji-linguistics/tree/main)

On the Lucene side, luckily, I'm fairly familiar how Lucene analyzers work due to my previous experiments in [lucene-grep](https://github.com/dainiusjocas/lucene-grep) tool.
The work there resulted in a couple of tiny [Clojure](https://clojure.org) libraries:
- [lucene-custom-analyzer](https://github.com/dainiusjocas/lucene-custom-analyzer) to build custom Lucene analyzers,
- [lucene-text-analysis](https://github.com/dainiusjocas/lucene-text-analysis) to play with Lucene analyzers.

### Plan

The key component for Vespa `Linguistics` (as per Javadoc) is the [`Tokenizer`](https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/linguistics/src/main/java/com/yahoo/language/process/Tokenizer.java).

>  <...>the tokenizer should typically stem, transform and normalize using the same operations as provided directly by this<...>

Output of the Tokenizer in Vespa is an [`Iterable`](https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html) of [`Token`s](https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/linguistics/src/main/java/com/yahoo/language/process/Token.java).
So, Lucene analyzer should be wrapped into a Vespa Tokenizer
and Lucene [`TokenStream`](https://github.com/apache/lucene/blob/main/lucene/core/src/java/org/apache/lucene/analysis/TokenStream.java#L78) should be converted into `Iterable<Token>`. 

### Building a Lucene Analyzer

I wanted a data-driven way to build Lucene analyzers because that would allow to store analyzer as a configuration in some file(s) in the Vespa application package without any custom Java code required.
To achieve this I've turned it a [`CustomAnalyzer`](https://github.com/apache/lucene/blob/538b7d0ffef7bb71dd214d7fb111ef787bf35bcd/lucene/analysis/common/src/java/org/apache/lucene/analysis/custom/CustomAnalyzer.java#L99) class.

`CustomAnalyzer` exposes a `Builder`.
`Builder` has convenient methods for adding Lucene `CharFilter`s, `Tokenizer`, and `TokenFilters` into an `Analyzer`.
They are added in methods with signatures:
- `public Builder addCharFilter(String name, Map<String, String> params)`
- `public Builder withTokenizer(String name, Map<String, String> params)`
- `public Builder addTokenFilter(String name, Map<String, String> params)`

The parameters of type `String` can be stored in a configuration file!

This way of building a `CustomAnalyzer` depends on the analysis component discovery through Java Service Provider Interface ([SPI](https://www.baeldung.com/java-spi)).
In practical terms this means that when libraries are prepared in certain way then functionality becomes available without explicit coding.
You can think of it as [plugins](https://en.wikipedia.org/wiki/Plug-in_%28computing%29).

Analyzer construction logic is encoded in the [`AnalyzerFactory`](https://github.com/dainiusjocas/journal/blob/main/src/journal/vespa/linguistics/lucene/src/main/java/lt/jocas/vespa/linguistics/AnalyzerFactory.java) class.

A tricky bit was to configure Vespa to load data from resource files.
Luckily, there is a `Builder` constructor that accepts `Path` parameter.

```c++
public static Builder builder(Path configDir)
```

And `Path` is the type exposed by the Vespa [configuration definition language](https://docs.vespa.ai/en/reference/config-files.html)!
Once deployed `Path` in the configuration points to where the files are stored in the Vespa `services` nodes, e.g. `/opt/vespa/var/db/vespa/filedistribution/dd3c228bd80aaf34/lucene`.
This allows to set relative paths in the Lucene config, e.g. stopwords dictionary files.
Success!

### Tokenizer implementation

The job here is to take the input `String`, apply the `Analyzer`, and then to convert the resulting `TokenStream` into `Iterable<Token>`.
The entire source [code](https://github.com/dainiusjocas/journal/blob/88b6d96ddb696aea9a5aee1b698ad1c5ac7d5f6e/src/journal/vespa/linguistics/lucene/src/main/java/lt/jocas/vespa/linguistics/LuceneTokenizer.java) as of now is as simple as:

```c++
private List<Token> textToTokens(String text, Analyzer analyzer) {
    List<Token> tokens = new ArrayList<>();
    TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);

    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
    OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
    try {
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String originalString = text.substring(offsetAttribute.startOffset(), offsetAttribute.endOffset());
            String tokenString = charTermAttribute.toString();
            tokens.add(new SimpleToken(originalString, tokenString)
                    .setType(TokenType.ALPHABETIC)
                    .setOffset(offsetAttribute.startOffset())
                    .setScript(TokenScript.UNKNOWN));
        }
        tokenStream.end();
        tokenStream.close();
    } catch (IOException e) {
        throw new RuntimeException("Failed to analyze: " + text, e);
    }
    return tokens;
}
```

Tokenizer decides how to process the input string according to three parameters:
```c++
public Iterable<Token> tokenize(String input, Language language, StemMode stemMode, boolean removeAccents)
```
So far Lucene analyzer are identified with a [`Language`](https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/linguistics/src/main/java/com/yahoo/language/Language.java) code.
I'm not entirely sure how to have more Analyzers than there are languages.

### `LuceneLinguistics` Configuration

Vespa relies on [dependency injection](https://docs.vespa.ai/en/jdisc/injecting-components.html) for preparing your application package.
Components might need to be [configured](https://docs.vespa.ai/en/configuring-components.html).
`LuceneLinguistics` is such a component.

Vespa exposes a [DSL](https://docs.vespa.ai/en/reference/config-files.html) for defining configuration.
For Lucene linguistics the configuration definition looks like [this](https://github.com/dainiusjocas/journal/blob/main/src/journal/vespa/linguistics/lucene/src/main/resources/configdefinitions/lucene-analysis.def):

```text
configDir                           path
analysis{}.tokenizer.name           string  default=standard
analysis{}.tokenizer.conf{}         string

analysis{}.charFilters[].name       string
analysis{}.charFilters[].conf{}     string
analysis{}.tokenFilters[].name      string
analysis{}.tokenFilters[].conf{}    string
```

Which is later converted to a Java class by the Vespa [Maven](https://maven.apache.org) [`bundle-plugin`](https://docs.vespa.ai/en/components/bundles.html#maven-bundle-plugin).
This class at runtime is injected into the `LuceneLinguistics`.

The actual configuration in the `services.xml` file [e.g.](https://github.com/dainiusjocas/journal/blob/main/src/journal/vespa/linguistics/vap/src/main/application/services.xml):
```xml
<component id="lt.jocas.vespa.linguistics.LuceneLinguistics" bundle="vespa-lucene-linguistics-poc">
  <config name="lt.jocas.vespa.linguistics.lucene-analysis">
    <configDir>lucene</configDir>
    <analysis>
      <item key="en">
        <tokenizer>
          <name>standard</name>
        </tokenizer>
        <tokenFilters>
          <item>
            <name>stop</name>
            <conf>
              <item key="words">en/stopwords.txt</item>
              <item key="ignoreCase">true</item>
            </conf>
          </item>
          <item>
            <name>englishMinimalStem</name>
          </item>
        </tokenFilters>
      </item>
    </analysis>
  </config>
</component>
```

It seems to be a bit verbose.
But the spirit of the analyzer configurability I wanted to achieve is like [here](https://github.com/dainiusjocas/lucene-custom-analyzer), e.g.:
```text
(custom-analyzer/create
  {:tokenizer :standard
   :char-filters [:htmlStrip]
   :token-filters [:uppercase]})
```
If there is a way to make it less verbose, let me know!

The only mandatory parameter is `configDir` (for which strangely it is not allowed to set a default value).
This parameter specified the directory in the application package relative to the package root which contains files for Lucene analysis components.
One inconvenient thing is that even if you don't need any configuration files, this param must be specified.

Another inconvenient thing is that the actual parameters for Lucene components are not easily available.
Sometimes, I hunt them down in the source code on Github.
Also, [Solr](https://solr.apache.org/guide/8_1/filter-descriptions.html) has nicely documented most of them, but the keys of analysis components are not there.

## Usage

The package is not properly released yet.
But it is coming soon.
For now you have to get the code from [here](https://github.com/dainiusjocas/journal/tree/main/src/journal/vespa/linguistics/lucene) and depending on your usage build it manually.
There are two ways how to use it.

### 1. As a component

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

### 2. As a dependency

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

Out of the box `LuceneLinguistics` provide many analysis components for you to use.
They are coming mostly from the [`lucene-analysis-common`](https://lucene.apache.org/core/9_7_0/analysis/common/index.html) library. 
In case you need some custom component `LuceneLinguistics` has you covered.
The examples assume that your application package contains custom code and is built with `maven`.

### Just add a maven dependency

As described in the design section the components are discovered via SPI.
So, in case you know a library that is readily packaged on Maven Central,
you can just add that dependency to your classpath and it will be available!

An example of such library is the [`stempel`](https://lucene.apache.org/core/8_10_1/analyzers-stempel/overview-summary.html).
It provides an algorithmic stemmer/lemmatizer for Polish language.
To use it, add a `dependency` in the `pom.xml` like this:

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
Let's not go into technicalities of doing that too much.
I'll just present a fairly involved example: a custom `TokenFilter`.
The plan is:
1. Implement the component, e.g. [here](https://github.com/dainiusjocas/journal/blob/main/src/journal/vespa/linguistics/vap/src/main/java/lt/jocas/vespa/linguistics/lemmagen/LemmagenTokenFilter.java),
2. Create a factory class implementation for the (1) that extends `TokenFilterFactory`, e.g. [here](https://github.com/dainiusjocas/journal/blob/main/src/journal/vespa/linguistics/vap/src/main/java/lt/jocas/vespa/linguistics/lemmagen/LemmagenTokenFilterFactory.java)
3. Register the implementation class for SPI, e.g. [here](https://github.com/dainiusjocas/journal/blob/main/src/journal/vespa/linguistics/vap/src/main/resources/META-INF/services/org.apache.lucene.analysis.TokenFilterFactory).

And we're done with implementation.

Now just `mvn package && vespa deploy`.
A nice benefit is that your shiny new Lucene analysis component is available for unit [testing](https://github.com/dainiusjocas/journal/blob/main/src/journal/vespa/linguistics/vap/src/test/java/lt/jocas/vespa/linguistics/lemmagen/LemmagenTokenFilterFactoryTest.java) as both:
the implementation classes themselves and part of `LuceneLinguistics`!

## Future work

- [ ] Default analyzers for some languages. Maybe some component registry?
- [ ] Documentation for the available analysis components?

## Conclusion

A Vespa `LuceneLinguistics` component allows you to **migrate** your Lucene based text analysis configuration into Vespa.
The overall design is flexible and extensible enough to cover the majority of Analyzers.
I hope this helps the transition from Lucene based search engines to Vespa and have fun while at it.

Feedback is very welcome! Let's chat [here](https://github.com/dainiusjocas/journal/issues/9).
