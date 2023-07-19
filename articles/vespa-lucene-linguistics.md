# Vespa Lucene Linguistics Implementation

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


TODO:Install into your local Maven repo:
```shell
mvn install:install-file -Dfile=target/vespa-lucene-linguistics-1.0.0-deploy.jar \
                         -DpomFile=pom.xml \
                         -DgroupId=lt.jocas \
                         -DartifactId=vespa-lucene-linguistics \
                         -Dversion=1.0.0 \
                         -Dpackaging=jar
```
