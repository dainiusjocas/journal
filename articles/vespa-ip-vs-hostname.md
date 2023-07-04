```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}

(ns vespa-ip-vs-hostname
  (:require
    [nextjournal.clerk :as clerk]
    [nextjournal.clerk.viewer :as v])
  (:import (java.time Instant)))

{:nextjournal.clerk/visibility {:code :hide :result :show}}
```
# Vespa Networking

## TL;DR

It is possible to use raw IPs instead of hostnames when setting up a [Vespa](https://vespa.ai) application.

## Intro

Vespa is a [distributed system](https://en.wikipedia.org/wiki/Distributed_computing).
Its [documentation](https://docs.vespa.ai/en/operations/node-setup.html) recommends to use [hostnames](https://docs.vespa.ai/en/operations/node-setup.html#hostname) when setting up an application.
The `hostname` internally is used for two purposes:
- as a node identifier;
- to lookup [IPs](https://en.wikipedia.org/wiki/IP_address).

## Can we use raw IPs directly?

If we deploy one Vespa node per machine then raw IPs would satisfy both uses above.
But why would you want to use a raw IP? 
One example would be when you want to generate `hosts.xml` and `services.xml` dynamically during deployment and your service discovery system only gives back IPs of Vespa nodes (e.g. [Consul](https://www.consul.io)).

But does it work to use IPs instead of hostnames?
Roll up your sleeves, make sure that Docker is set up, vespa CLI is installed, and let's find out.

The demo application package can be found [here](https://github.com/dainiusjocas/vespa-networking-sandbox).


## Experiment

As a base, let's take the sample app from the [multinode HA](https://github.com/vespa-engine/sample-apps/tree/master/examples/operations/multinode-HA) project
and modify it so that the Vespa deployment would have
- 3 configservers (3 because each services node must include all the configserver hostnames),
- 1 services nodes.

Let's run the above setup in Docker.
The Vespa [application package](https://docs.vespa.ai/en/application-packages.html) will be using the static IPs.
Then let's deploy the APP and check if everything is working as expected.

### Prepare Docker

```clojure
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def gateway "172.30.0.1")
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def hosts ["172.30.0.2" "172.30.0.3" "172.30.0.4" "172.30.0.5"])
```

First, we need to create a Docker bridge network
(and from now on the setup expects that the following network setup works).

```clojure
(clerk/html
  [:pre
   (format "docker network create \\
   --driver bridge \\
   --subnet=172.30.0.0/16 \\
   --ip-range=172.30.0.0/24 \\
   --gateway %s \\
   vespanet" gateway)])
```

We need to run 4 containers, so let's give them static IPs:

```
hosts
```

From these 3 are dedicated to configservers 
```clojure
(into [] (take 3) hosts)
```

This means that the `VESPA_CONFIGSERVERS` env var value is going to be 
```clojure
(clojure.string/join " " (into [] (take 3) hosts))
```

### Start Vespa nodes

For a Vespa node to use a raw IP we need a little hack: to set an environment variable
`VESPA_HOSTNAME` with an IP of the container.
It works because the `VESPA_HOSTNAME` value takes precedence over taking value from `hostname` when registering with the configserver.

Let's start all 3 configservers:
```clojure
(clerk/html
  [:pre
   "
 docker run --detach --name node0 --hostname node0.vespanet \\
   -e VESPA_CONFIGSERVERS=\"172.30.0.2 172.30.0.3 172.30.0.4\" \\
   -e VESPA_CONFIGSERVER_JVMARGS=\"-Xms32M -Xmx128M\" \\
   -e VESPA_CONFIGPROXY_JVMARGS=\"-Xms32M -Xmx32M\" \\
   -e VESPA_HOSTNAME=\"172.30.0.2\" \\
   --network vespanet \\
   --ip 172.30.0.2 \\
   --publish 19071:19071 --publish 19100:19100 --publish 19050:19050 --publish 20092:19092 \\
   vespaengine/vespa configserver,services
"])
```
```clojure
(clerk/html
  [:pre
   "
 docker run --detach --name node1 --hostname node1.vespanet \\
   -e VESPA_CONFIGSERVERS=\"172.30.0.2 172.30.0.3 172.30.0.4\" \\
   -e VESPA_CONFIGSERVER_JVMARGS=\"-Xms32M -Xmx128M\" \\
   -e VESPA_CONFIGPROXY_JVMARGS=\"-Xms32M -Xmx32M\" \\
   -e VESPA_HOSTNAME=\"172.30.0.3\" \\
   --network vespanet \\
   --ip 172.30.0.3 \\
   --publish 19072:19071 --publish 19101:19100 --publish 19051:19050 --publish 20093:19092 \\
   vespaengine/vespa configserver,services
"])
```

```clojure
(clerk/html
  [:pre
   "
 docker run --detach --name node2 --hostname node2.vespanet \\
   -e VESPA_CONFIGSERVERS=\"172.30.0.2 172.30.0.3 172.30.0.4\" \\
   -e VESPA_CONFIGSERVER_JVMARGS=\"-Xms32M -Xmx128M\" \\
   -e VESPA_CONFIGPROXY_JVMARGS=\"-Xms32M -Xmx32M\" \\
   -e VESPA_HOSTNAME=\"172.30.0.4\" \\
   --network vespanet \\
   --ip 172.30.0.4 \\
   --publish 19073:19071 --publish 19102:19100 --publish 19052:19050 --publish 20094:19092 \\
   vespaengine/vespa configserver,services
"])
```

Check if all are up:
```clojure
(clerk/html
  [:pre
   "
 ( for port in 19071 19072 19073; do \\
   curl -s http://localhost:$port/state/v1/health | head -5; \\
   done )
"])
```

Start the services container:

```clojure
(clerk/html
  [:pre
   "
 docker run --detach --name node3 --hostname node3.vespanet \\
   -e VESPA_CONFIGSERVERS=\"172.30.0.2 172.30.0.3 172.30.0.4\" \\
   -e VESPA_CONFIGSERVER_JVMARGS=\"-Xms32M -Xmx128M\" \\
   -e VESPA_CONFIGPROXY_JVMARGS=\"-Xms32M -Xmx32M\" \\
   -e VESPA_HOSTNAME=\"172.30.0.5\" \\
   --network vespanet \\
   --ip 172.30.0.5 \\
   --publish 8080:8080 --publish 19092:19092 \\
   vespaengine/vespa services
"])
```

### Deploy the application package

Deploy the sample [Vespa application package](https://github.com/dainiusjocas/vespa-networking-sandbox):

The `hosts.xml` file has the following [content](https://github.com/dainiusjocas/vespa-networking-sandbox/blob/main/vap/hosts.xml):

```clojure
^{:nextjournal.clerk/width :full}
(clerk/html
  [:pre (slurp "src/journal/vespa/networking/vap/hosts.xml")])
```

Deploy:

```clojure
^{:nextjournal.clerk/width :full}
(clerk/html
  [:pre
   "vespa deploy -w 300
Uploading application package ... done

Success: Deployed .
WARNING Host named '172.30.0.5' may not receive any config since it differs from its canonical hostname 'node3.vespanet' (check DNS and /etc/hosts).
WARNING Host named '172.30.0.2' may not receive any config since it differs from its canonical hostname 'node0.vespanet' (check DNS and /etc/hosts).
WARNING Host named '172.30.0.3' may not receive any config since it differs from its canonical hostname 'node1.vespanet' (check DNS and /etc/hosts).
WARNING Host named '172.30.0.4' may not receive any config since it differs from its canonical hostname 'node2.vespanet' (check DNS and /etc/hosts).

Waiting up to 5m0s for query service to become available ...
"])
```

There the warnings but it is fine.
We could get rid of it by setting the hostname to the IP value.

Let's check if the app is up?

```clojure
(clerk/html [:pre "curl -s http://localhost:8080/state/v1/health | jq '.status'"])
```

### Test the set up

Feed a document:

```clojure
^{:nextjournal.clerk/width :full}
(clerk/html
  [:pre
   "
  curl -X POST -H \"Content-Type:application/json\" --data '
  {
    \"fields\": {
      \"artist\": \"Coldplay\",
      \"album\": \"A Head Full of Dreams\",
      \"year\": 2015
    }
  }' \\
  http://localhost:8080/document/v1/mynamespace/music/docid/a-head-full-of-dreams
   "])
```

Search for the indexed documents:
```clojure
^{:nextjournal.clerk/width :full}
(clerk/html
  [:pre
   "
  curl -X POST -H \"Content-Type: application/json\" --data '
  {
      \"yql\": \"select * from sources * where true\"
  }' \\
  http://localhost:8080/search/
   "])
```

It returns the document that we've just fed. Victory!

## Summary

We've set up Vespa system so that it has 3 configserver and 1 services node and they all are running in the `vespanet` Docker network.
The configuration has all IPs set explicitly.
Also, we've deployed an application package that instead of hostnames contains raw IPs and tested it. 

It is possible to use IPs instead of hostname for Vespa applications.
