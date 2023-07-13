# Vespa Lucene Linguistics

### Configuration

In your `services.xml` file:

```xml

<container id="mycontainer" version="1.0">
  <component id="lt.jocas.vespa.linguistics.LuceneLinguistics" bundle="vespa-lucene-linguistics">
    <config name="lt.jocas.vespa.linguistics.lucene-analysis">
      <analysis>
        <item key="en">
          <tokenizer>
            <name>standard</name>
          </tokenizer>
        </item>
      </analysis>
    </config>
  </component>
</container>
```
