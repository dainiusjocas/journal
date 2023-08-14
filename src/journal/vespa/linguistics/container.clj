(ns journal.vespa.linguistics.container
  (:require [clj-test-containers.core :as tc]))

(def vap-dir "/opt/vespa/testcontainers")

(defn package! [in-container]
  (tc/execute-command!
    in-container
    ["sh" "-c" (format "(cd %s && mvn clean -DskipTests package)" vap-dir)]))

(defn deploy! [in-container]
  (tc/execute-command!
    in-container
    ["sh" "-c" (format "(cd %s && vespa deploy -w 60)" vap-dir)]))

(defn build-vap-and-deploy! [in-container]
  [(doto (package! in-container) println)
   (doto (deploy! in-container) println)])

(def vespa
  (delay
    (doto (-> (tc/create-from-docker-file
                {:docker-file   "src/journal/vespa/linguistics/Dockerfile"
                 :exposed-ports [8080 19050 19071 19092]
                 :env-vars      {"VESPA_LOG_FORMAT" "json"}
                 :wait-for      {:wait-strategy   :http
                                 :path            "/"
                                 :port            19071
                                 :method          "GET"
                                 :status-codes    [200]
                                 :tls             false
                                 :read-timout     5
                                 :headers         {"Accept" "text/plain"}
                                 :startup-timeout 300}
                 :log-to        {:log-strategy :fn
                                 :function     (fn [log-line]
                                                 (when (re-matches #"(?i).*lucene.*" log-line)
                                                   (print "VESPA>: " log-line)))}})
              (tc/bind-filesystem! {:host-path      "src/journal/vespa/linguistics/vap2"
                                    :container-path vap-dir
                                    :mode           :read-write})
              (tc/start!))
      (build-vap-and-deploy!))))

(defn restart-services! [in-container]
  (tc/execute-command!
    in-container
    ["sh" "-c" "/opt/vespa/bin/vespa-stop-services && /opt/vespa/bin/vespa-start-services"]))
