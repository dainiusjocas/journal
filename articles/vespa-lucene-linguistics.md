# Vespa Lucene Linguistics Implementation

The goal is use Lucene text analysis components in place of Vespa linguistics.

This helps the transition from Lucene based search engines to Vespa.

The inspiration is from:
- [vespa-chinese-linguistics](https://github.com/vespa-engine/sample-apps/blob/master/examples/vespa-chinese-linguistics/src/main/java/com/qihoo/language/JiebaLinguistics.java).
- OpenNlp Linguistics https://github.com/vespa-engine/vespa/blob/50d7555bfe7bdaec86f8b31c4d316c9ba66bb976/opennlp-linguistics/src/main/java/com/yahoo/language/opennlp/OpenNlpLinguistics.java


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

# Configuration


