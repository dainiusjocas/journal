FROM vespaengine/vespa:8.194.16

USER root

RUN curl https://dlcdn.apache.org/maven/maven-3/3.9.3/binaries/apache-maven-3.9.3-bin.zip -o /opt/apache-maven-3.9.3-bin.zip && \
    unzip /opt/apache-maven-3.9.3-bin.zip -d /opt && \
    mkdir /opt/vespa/.m2/ && \
    chown vespa /opt/vespa/.m2/

USER vespa

ENV JAVA_HOME="/usr/lib/jvm/java-17"
ENV PATH="$PATH:/opt/apache-maven-3.9.3/bin"
