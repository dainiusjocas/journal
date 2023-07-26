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
(cd ../lucene && mvn clean install)

mvn clean package
vespa deploy -w 100

vespa feed src/main/application/ext/document.json
vespa query 'yql=select * from lucene where default contains "something"'
# observe the lowercased and stemmed query string when compared with the text from document.
```

The query should return:
```json
{
    "root": {
        "id": "toplevel",
        "relevance": 1.0,
        "fields": {
            "totalCount": 1
        },
        "coverage": {
            "coverage": 100,
            "documents": 2,
            "full": true,
            "nodes": 1,
            "results": 1,
            "resultsFull": 1
        },
        "children": [
            {
                "id": "id:mynamespace:lucene::mydocid",
                "relevance": 0.37754900648443235,
                "source": "content",
                "fields": {
                    "sddocname": "lucene",
                    "documentid": "id:mynamespace:lucene::mydocid",
                    "mytext": "sOMETHINGs Zero One Two Three Four Five Six Seven Eight"
                }
            }
        ]
    }
}
```

Voila!

## Search behaviour is a bit unexpected!

If the configured analyzer for e.g. reverses or lowercases strings, then what is actually written into posting lists are not
the reversed strings!
While in search the string after the tokenizer are used. 
This creates asymmetry in text processing.
