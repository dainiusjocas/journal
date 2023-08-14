```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(ns vespa-searcher-properties
  {:nextjournal.clerk/toc        :collapsed
   :nextjournal.clerk/open-graph {:image       "https://docs.vespa.ai/assets/logos/vespa-logo-full-black.svg"
                                  :description "Notes on Vespa Lucene Linguistics."}}
  (:require
    [clojure.string :as str]
    [journal.utils.jsoup :as jsoup]
    [nextjournal.clerk :as clerk]
    [nextjournal.clerk.viewer :as v]
    [journal.vespa.searcher.core :as searcher]))

{:nextjournal.clerk/visibility {:code :hide :result :show}}
```
# Vespa Lucene Linguistics Analyzer Priority

#### 2023-08-14

## TL;DR

The per-language text analysis configuration priority is as follows:
- `LuceneLinguistics` component configuration,
- `ComponentsRegistry`,
- Language default analyzers;
- the `StandardAnalyzer`.

## Context

Vespa uses a [linguistics](https://docs.vespa.ai/en/linguistics.html) module to process text in queries and documents during indexing and searching.
The [Lucene](https://lucene.apache.org) based `Linguistics` implementation, the [`LuceneLinguistics`](https://github.com/vespa-engine/vespa/tree/master/lucene-linguistics) is highly configurable.
In your Vespa application package (VAP) there are 2 ways to set up the language handling:
1. `LuceneLinguistics` component configuration,
2. `ComponentsRegistry` of Lucene [`Analyzer`s](https://lucene.apache.org/core/9_7_0/core/org/apache/lucene/analysis/package-summary.html#package.description).

This article explores those 2 options.

## `LuceneLinguistics` component configuration

The `LuceneLinguistics` component accepts configuration for the language handling.
The output of the configuration is an instance of a Lucene `Analyzer` per language.
Analyzers created with this configuration has the highest priority over other available analyzers per language.

Example:
```xml
<components>
  <component id="linguistics"
             class="com.yahoo.language.lucene.LuceneLinguistics"
             bundle="vespa-lucene-linguistics-poc">
    <config name="com.yahoo.language.lucene.lucene-analysis">
      <configDir>linguistics</configDir>
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
</components>
```

The example above configures a [`CustomAnalyzer`](https://lucene.apache.org/core/9_7_0/analysis/common/org/apache/lucene/analysis/custom/CustomAnalyzer.html) for the English language, which uses the [`standard`](https://lucene.apache.org/core/9_7_0/core/org/apache/lucene/analysis/standard/StandardTokenizer.html) tokenizer, the `stop` token filter that loads
the stopwords list from a file `en/stopwords.txt` from your VAP, and then applies the `englishMinimalStem` on the tokens.

However, the configuration with XML is a bit verbose and your IDE can't help much preventing mistakes.
Also, to check if it really works as expected you need to deploy the application package, which is not an instant experience.

For unit-testing [Vespa offers to programmatically build configuration instances](https://docs.vespa.ai/en/unit-testing.html#unit-testing-configurable-components).
This is helpful to test your component **during the component development**, but relies on copy-pasting config values from the `services.xml` when testing the values for the **actual VAP deployment**.
Of course, you can implement a little helper that extracts values from XML and builds configuration instance. 

For the `LuceneLinguistics` design details check [this article](https://www.jocas.lt/journal/articles/vespa-lucene-linguistics/), the source code is [here](https://github.com/vespa-engine/vespa/tree/master/lucene-linguistics).

## Analyzers built as Vespa Components

Vespa provides a `ComponentRegistry` mechanism via [JDisc](https://docs.vespa.ai/en/jdisc/).
The `LuceneLinguistics` accepts a [ComponentRegistry<Analyzer>](https://docs.vespa.ai/en/jdisc/injecting-components.html#depending-on-all-components-of-a-specific-type) into its constructor.
This allows you to create a custom `Analyzer` in Java and then register it as a Vespa Component in the [`services.xml`](https://docs.vespa.ai/en/reference/services.html).

The main advantages of this approach is are:
- development experience: IDE helps you with the code completion, source code, and type safety;
- resource files can be loaded from the [classpath](https://docs.oracle.com/javase/tutorial/essential/environment/paths.html), e.g. the [`resources`](https://stackoverflow.com/questions/25786185/what-is-the-purpose-for-the-resource-folder-in-maven) directory;
- testability: [JUnit](https://junit.org/junit5/) is your friend;
- there is a `Lucene in Action` [book](https://www.amazon.com/Lucene-Action-Second-Covers-Apache/dp/1933988177/ref=sr_1_1), and chapter 4 is dedicated to text analysis,
- the internet is full of guides how to build Lucene analyzers, e.g. [here](https://www.baeldung.com/lucene-analyzers#custom).

When it comes to complexity, there are two types of `Analyzer` components:
1. That doesn't require any setup.
2. That requires some setup (e.g. constructor with arguments).

### When no set up is required

To use your custom `Analyzer` that is available in the classpath all you need is to declare Analyzer components in the `services.xml`, e.g.:
```xml
<component id="en"
           class="org.apache.lucene.analysis.core.SimpleAnalyzer"
           bundle="vespa-lucene-linguistics-poc" />
```

Where:
- `id` must be a [`Language`](https://github.com/vespa-engine/vespa/blob/873350caf5e984b5a580e2e0585dfd521eb493c0/linguistics/src/main/java/com/yahoo/language/Language.java#L505) code.
- `class` should be the implementation class.
  Note that in this example the class is taken straight from the Lucene library!
  Also, you can create an `Analyzer` class directly inside your VAP and refer it, more about it in a second.
- `bundle` must be your application package's `artifactId` as specified in the `pom.xml`.
  - Or can be any bundle added to your VAP `components` dir that contains the `class`.

For this to work, the class must **only** provide a constructor without arguments.

### When set up is required

Some analyzers require a bit more work to be used as components.
Namely, you need to create a class that implements the [`Provider`](https://docs.vespa.ai/en/jdisc/injecting-components.html#special-components) interface and wraps your `Analyzer`.
Then you can add the `component` of the wrapper class to the `services.xml`.

When would that be useful?
For cases when you get an implementation from the 3rd party library, e.g. 
there is a Lucene library [`lucene-analysis-stempel`](https://lucene.apache.org/core/9_7_0/analysis/stempel/index.html) that supports the Polish language.
Let's work out this example.

First, add the `lucene-analysis-stempel` library to your `pom.xml`
```xml
<dependency>
  <groupId>org.apache.lucene</groupId>
  <artifactId>lucene-analysis-stempel</artifactId>
  <version>9.7.0</version>
</dependency>
```

Second, create a little wrapper class:
```c++
package ai.vespa.linguistics.pl;

import com.yahoo.container.di.componentgraph.Provider;
import org.apache.lucene.analysis.Analyzer;

public class PolishAnalyzer implements Provider<Analyzer> {
    @Override
    public Analyzer get() {
        return new org.apache.lucene.analysis.pl.PolishAnalyzer();
    }
    @Override
    public void deconstruct() {}
}
```

Third, add the component declaration into the containers `services.xml` file:
```xml
<component id="pl"
           class="ai.vespa.linguistics.pl.PolishAnalyzer"
           bundle="vespa-lucene-linguistics-poc" />
```
All set up! Now you have a Polish language analysis set up!

You may ask, why does `org.apache.lucene.analysis.pl.PolishAnalyzer` require set up, as the constructor is without parameters?
Because the class also has constructors with arguments and the `ComponentRegistry` doesn't know which to use.

## Default analyzers per language

`LuceneLinguistics` sets a default analyzer for some languages.
These analyzers (as of 8.204.11) come with the `lucene-analysis-common` library.
You can find the full list [here](https://github.com/vespa-engine/vespa/blob/master/lucene-linguistics/src/main/java/com/yahoo/language/lucene/DefaultAnalyzers.java).
It should serve as a solid base for implementing lexical search functionality.

How to expand the list of languages with the default analyzer?
Create a pull request for Vespa ;)

## The fallback Analyzer

If there is no analyzer specified in any way described above for some language, then the text will be processed with the `StandardAnalyzer`.

## On stemming

`LuceneLinguistics` (as of 8.204.11) ignores any [stemming](https://docs.vespa.ai/en/linguistics.html#stemming) settings at both: index and query time.
To prevent unnecessary work you should disable stemming so that even the noop stemmer is not applied.
E.g. add to your schema file `stemming: none`.

How much of a problem it is?
It depends, but in any case you should [apply filter by language when querying](https://docs.vespa.ai/en/linguistics.html#querying-with-language) to increase precision.

## On the language prediction

You must provide the language of the document field or the query because Lucene (as of 8.204.11) doesn't predict the language.

## Conclusion

The `LuceneLinguistics` implementation turned out to be highly configurable.
It supports 2 ways to implement the language handling, sensible defaults, and a fallback.
I hope this article helps to set up text handling in your Vespa applications.

Feedback is very welcome! Let's chat in the [Vespa Slack](http://slack.vespa.ai/) or [here](https://github.com/dainiusjocas/journal/issues/9).
