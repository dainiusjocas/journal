# Vespa Lucene Linguistics

This is a very early prototype!
Use it at your own risk.

## Development

Build:
```shell
mvn clean test -U package
```

To compile configuration classes so that Intellij doesn't complain:
- right click on `pom.xml` 
- then `Maven` 
- then `Generate Sources and Update Folders`

## Usage

Add `<component>` to `services.xml` of your application package, e.g.:
```xml
<component id="lt.jocas.vespa.linguistics.LuceneLinguistics" bundle="vespa-lucene-linguistics">
  <config name="lt.jocas.vespa.linguistics.lucene-analysis">
    <analysis>
      <item key="en">
        <tokenizer>
          <name>standard</name>
        </tokenizer>
        <tokenFilters>
          <item>
            <name>reverseString</name>
          </item>
        </tokenFilters>
      </item>
    </analysis>
  </config>
</component>
```
into `container` clusters that has `<document-processing/>` and/or `<search>` specified.

Copy the `target/vespa-lucene-linguistics-1.0.0-deploy.jar` into your application package `components` folder.

And then package and deploy, e.g.:
```shell
(mvn clean -DskipTests=true -U package && vespa deploy -w 100)
```

### Configuration of Lucene Analyzers

Read the Lucene docs of subclasses of:
- [TokenizerFactory](org.apache.lucene.analysis.TokenizerFactory), e.g. [StandardTokenizerFactory](https://lucene.apache.org/core/9_0_0/core/org/apache/lucene/analysis/standard/StandardTokenizerFactory.html)
- [CharFilterFactory](https://lucene.apache.org/core/9_0_0/core/org/apache/lucene/analysis/CharFilterFactory.html), e.g.  [PatternReplaceCharFilterFactory](https://lucene.apache.org/core/8_1_1/analyzers-common/org/apache/lucene/analysis/pattern/PatternReplaceCharFilterFactory.html)
- [TokenFilterFactory](https://lucene.apache.org/core/8_1_1/analyzers-common/org/apache/lucene/analysis/util/TokenFilterFactory.html), e.g. [ReverseStringFilterFactory](https://lucene.apache.org/core/8_1_1/analyzers-common/org/apache/lucene/analysis/reverse/ReverseStringFilterFactory.html)

E.g. tokenizer `StandardTokenizerFactory` has this config [snippet](https://lucene.apache.org/core/9_0_0/core/org/apache/lucene/analysis/standard/StandardTokenizerFactory.html):
```xml
 <fieldType name="text_stndrd" class="solr.TextField" positionIncrementGap="100">
   <analyzer>
     <tokenizer class="solr.StandardTokenizerFactory" maxTokenLength="255"/>
   </analyzer>
 </fieldType>
```

Then go to the [source code](https://github.com/apache/lucene/blob/17c13a76c87c6246f32dd7a78a26db04401ddb6e/lucene/core/src/java/org/apache/lucene/analysis/standard/StandardTokenizerFactory.java#L36) of the class on Github.
Copy value of the `public static final String NAME` into the `<name>` and observe the names used for configuring the tokenizer (in this case only `maxTokenLength`).
```xml
<tokenizer>
  <name>standard</name>
  <config>
    <item key="maxTokenLength">255</item>
  </config>
</tokenizer>
```

The `AnalyzerFactory` constructor logs the available analysis components.

The analysis components are discovered through Java Service Provider Interface (SPI).
To add more analysis components it should be enough to put a Lucene analyzer dependency into your application package `pom.xml`
or register services and create classes directly in the application package.

### Resource files

TODO: work out the details (doesn't work when deployed).
But resource loading e.g. stopwords.txt file, works in tests.

## Inspiration

These projects:
- [vespa-chinese-linguistics](https://github.com/vespa-engine/sample-apps/blob/master/examples/vespa-chinese-linguistics/src/main/java/com/qihoo/language/JiebaLinguistics.java).
- OpenNlp Linguistics https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/opennlp-linguistics/src/main/java/com/yahoo/language/opennlp/OpenNlpLinguistics.java
- [vespa-kuromoji-linguistics](https://github.com/yahoojapan/vespa-kuromoji-linguistics/tree/main)
- [Clojure library](https://github.com/dainiusjocas/lucene-text-analysis) to work with Lucene analyzers 
