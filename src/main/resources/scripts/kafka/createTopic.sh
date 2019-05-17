#!/usr/bin/env bash
# see https://hub.docker.com/r/spotify/kafka

topic=$1
DIR=`dirname $0`
source "$DIR/"dockerFunctions.sh

echo "Creating topic '$topic' in $CONTAINER_NAME "

docker exec -t "${CONTAINER_NAME}" /opt/kafka_2.12-2.2.0/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic "$topic"