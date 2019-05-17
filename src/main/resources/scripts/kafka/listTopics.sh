#!/usr/bin/env bash
# see https://hub.docker.com/r/spotify/kafka

DIR=`dirname $0`
source "$DIR/"dockerFunctions.sh

echo "topics for $CONTAINER_NAME are..."
docker exec -t "${CONTAINER_NAME}" /opt/kafka_2.12-2.2.0/bin/kafka-topics.sh --list --zookeeper localhost:2181