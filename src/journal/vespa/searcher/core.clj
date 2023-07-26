(ns journal.vespa.searcher.core
  (:require [journal.utils.json :as json]
            [clojure.string :as str]
            [clojure.test :refer [is]]
            [clj-test-containers.core :as tc]
            [org.httpkit.client :as http]
            [journal.vespa.searcher.container :as container]))


;; start docker container

(defn start! [] (tc/start! @container/vespa))

(defn stop! [] (tc/stop! @container/vespa))

(defn vap-container-port [] (get (:mapped-ports @container/vespa) 8080))

(defn configserver-port [] (get (:mapped-ports @container/vespa) 19071))

(defn status-in-docker []
  (tc/execute-command! @container/vespa ["sh" "-c" "/opt/vespa/bin/vespa status"]))

(defn run-query []
  (tc/execute-command!
    @container/vespa
    ["sh" "-c" "vespa query \"select * from music where album contains 'head'\""]))

(defn build-and-deploy! []
  (container/build-vap-and-deploy! @container/vespa))

(defn http-call
  ([] (http-call {"foo_priority" "query_param_value"}
                 {"Content-Type"      "application/json"
                  "foo-headers-key" "foo-headers-value"}
                 {"foo_priority"         "body_value"
                  "foo_int"              100
                  "foo_float"            100.1
                  "foo_string"           "my_string"
                  "foo_array_of_scalars" [1 2 3]
                  "foo_array_of_objects" [{"bar" "baz"} {"aaa" "bbb"}]}))
  ([query-params headers body]
   (http-call query-params headers body (vap-container-port)))
  ([query-params headers body port]
   @(http/request
      {:method       :post
       :url          (format "http://localhost:%s/search/" port)
       :query-params query-params
       :headers      (if (empty? headers)
                       {"Content-Type" "application/json"}
                       headers)
       :body         (json/json-str body)})))

(defn http-resp-pprint [resp]
  (-> resp
      (update :body json/read-str)
      (dissoc :opts)
      json/pprint))

(defn resp-to-curl [resp]
  (let [opts (:opts resp)
        hdrs (fn [headers]
               (str/join " \\\n  "
                         (mapv (fn [[k v]]
                                 (str "-H \"" k ": " v "\"")) headers)))
        qp (fn [query-params]
             (when-not (empty? query-params)
               (str "?"
                    (str/join "&" (mapv (fn [[k v]] (str k "=" v)) query-params)))))]
    (format
      "curl -s -X POST %s --data '\n%s' \\\nhttp://localhost:8080/search/%s"
      (hdrs (:headers opts))
      (str/trim (json/pprint (json/read-str (:body opts))))
      (or (qp (:query-params opts)) ""))))

(defn query-flat-dot []
  (http-call {} {} {"model.queryString" "head"}))

(defn query-nested-json []
  (http-call {} {} {"model" {"queryString" "head"}}))

(comment
  (in-ns 'journal.vespa.searcher.core)
  (-> (http-call)
      (update :body json/read-str))

  ; The content length differs in request, responses are the same
  (is (= (get (json/read-str (:body (query-flat-dot))) "body")
         (get (json/read-str (:body (query-nested-json))) "body"))))

;; ## TODO
;
;- (LEAVE OUT) Defaults Searcher Chains
;- (DONE) Rework queries to take data from a file
;- (DOESNT WORK) Add giscus integration
;- (DONE) Mention that it is possible to put stuff into headers, but you need a build flag.
;- (DONE) Create a function that creates curl from data files.
;- (DONE) take XML snippets with jsoup from config
;- (DONE) Make one request with various dummy structures, and make compound names for each
;- (DONE) README to make stuff run
;- (DONE) make the point that URL params also are smashed into properties
;  - (QUERY STRING) what priority is between params: url? body? flat? And the query params takes precedence
;- (OOS) can we modify the deployer so that it modifies the zip file? i.e. writes hosts.xml and services.xml inside and deploys?
;- +Rich Hickey rant about HTTP request response being a map
;- What is strict? https://docs.vespa.ai/en/reference/query-profile-reference.html#strict
