# Vespa Lucene Linguistics POC Application Package

Tested on M1 Mac.

```shell
# Of course make sure that your Docker daemon is running
# make sure that Vespa CLI is installed
brew install vespa-cli
# Maven must be 3.6+
brew install maven

docker run --rm --detach \
  --name vespa \
  --hostname vespa-container \
  --publish 8080:8080 \
  --publish 19071:19071 \
  --publish 19050:19050 \
  vespaengine/vespa:8.194.16

# In case you want to see logs
# open up a separate terminal
# run `docker logs vespa -f`

vespa status deploy --wait 300

# Package the Lucene Linguistics implementation
(cd ../lucene && mvn clean package)

mvn clean package
vespa deploy -w 100

vespa feed src/main/application/ext/document.json
vespa query "select * from lucene where true"
```

In the logs you should be able to find something like this:
```text
[2023-07-19 14:03:57.751] ERROR   container        Container.lt.jocas.vespa.linguistics.LuceneTokenizer	Tokenized text='Vespa Lucene Linguistics sample document' into: n=5, tokens=[token 'apseV', token 'enecuL', token 'scitsiugniL', token 'elpmas', token 'tnemucod']
```

Voila!

## Search doesn't not work as expected!

The configured analyzer for English language tokenizes and reverses strings.
The `reverseString` token filter creates problems for searching because for some reason it reverses the query terms two time.
This is only issue for this demo, change the analyzer to something more reasonable and searching will work.
