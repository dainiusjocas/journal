(ns journal.vespa.searcher.container
  (:require [clj-test-containers.core :as tc]))

(def vap-dir "/opt/vespa/testcontainers")

(def scripts-dir "/opt/vespa/scripts")

(defn package! [in-container]
  (tc/execute-command!
    in-container
    ["sh" "-c" (format "(cd %s && mvn -DskipTests -U package)" vap-dir)]))

(defn deploy! [in-container]
  (tc/execute-command!
    in-container
    ["sh" "-c" (format "(cd %s && vespa deploy -w 120)" vap-dir)]))
(defn build-vap-and-deploy! [in-container]
  [(package! in-container)
   (println ">>>AFTER PACKAGE")
   (deploy! in-container)
   (println ">>>AFTER DEPLOY")])

(def vespa
  (delay
    (doto (-> (tc/create-from-docker-file
                {:docker-file   "src/journal/vespa/searcher/Dockerfile"
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
                                 :function     (fn [log-line] (print "VESPA>: " log-line))}})
              (tc/bind-filesystem! {:host-path      "src/journal/vespa/searcher/vap"
                                    :container-path vap-dir
                                    :mode           :read-write})
              (tc/bind-filesystem! {:host-path      "src/journal/vespa/searcher/scripts"
                                    :container-path scripts-dir
                                    :mode           :read-write})
              (tc/start!))
      (println ">>>>Vespa container has started!")
      (build-vap-and-deploy!)
      (println ">>>>VAP was deployed!"))))

(defn restart-services! []
  (tc/execute-command!
    @vespa
    ["sh" "-c" "/opt/vespa/bin/vespa-stop-services && /opt/vespa/bin/vespa-start-services"]))
