#
# This was taken from the now very old spotify/kafka image which spins up kafka and zookeeper.
# Others are available, but this was the snappiest to start/stop I found, so I've just repurposed
# it to use the java stretch base and latest kafka.
#
# 23 March 2020: This has been published as porpoiseltd/kafka-2.5.0_2.13 so the kafa start/stop no longer has to
# *ahem* build the full image
FROM openjdk:11-jre-stretch

ENV DEBIAN_FRONTEND noninteractive
ENV SCALA_VERSION 2.13
ENV KAFKA_VERSION 2.8.0
ENV KAFKA_HOME /opt/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION"

ADD scripts/start-kafka.sh /usr/bin/start-kafka.sh

# Install Kafka, Zookeeper and other needed things
RUN apt-get update && \
    apt-get install -y zookeeper wget supervisor dnsutils vim && \
    rm -rf /var/lib/apt/lists/*  && \
    apt-get clean && \
    wget -q http://apache.mirrors.spacedump.net/kafka/"$KAFKA_VERSION"/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -O /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz && \
    tar xfz /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -C /opt && \
    rm /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz && \
    chmod +x /usr/bin/start-kafka.sh

# Supervisor config
ADD supervisor/kafka.conf supervisor/zookeeper.conf supervisor/rest.conf /etc/supervisor/conf.d/

# 2181 is zookeeper, 8083 and 9092 are kafka (REST, client API), 9080 is systemd as per supervisor/rest.conf
EXPOSE 2181 8083 9092 9080

CMD ["supervisord", "-n"]