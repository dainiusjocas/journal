FROM vespaengine/vespa:8.204.11

USER root

ENV MAVEN_VERSION=3.9.4

RUN curl https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.zip -o /opt/apache-maven-${MAVEN_VERSION}-bin.zip && \
    unzip /opt/apache-maven-${MAVEN_VERSION}-bin.zip -d /opt && \
    mkdir /opt/vespa/.m2/ && \
    chown vespa /opt/vespa/.m2/

USER vespa

ENV JAVA_HOME="/usr/lib/jvm/java-17"
ENV PATH="$PATH:/opt/apache-maven-${MAVEN_VERSION}/bin"
