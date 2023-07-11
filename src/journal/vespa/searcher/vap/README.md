# Vespa Echo Searcher Demo

Start Vespa on your machine in the Docker container:

```shell
# make sure that Vespa CLI is installed
brew install vespa-cli
# Maven must be 3.6+
brew install maven

docker run --rm --name vespa --hostname vespa-container \
  --publish 8080:8080 --publish 19071:19071 \
  vespaengine/vespa:8.188.15
vespa status deploy --wait 300
# This takes about 20 seconds
mvn -U package
vespa deploy --wait 60
```

Similar to the [Quick Start, with Java](https://docs.vespa.ai/en/vespa-quick-start-java.html)

Your app should be ready!

```shell
curl -s -X POST -H "Content-Type: application/json" --data '
{
  "model.queryString": "head"
}' \
http://localhost:8080/search/
```

Happy hacking!
