#!/usr/bin/env bash

DIR=`dirname $0`
source "$DIR/"dockerFunctions.sh

topic=$1
echo "tailing $topic in $CONTAINER_NAME"

docker exec -t "$CONTAINER_NAME" /opt/kafka_2.13-2.5.0/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic "$topic" \
  --from-beginning
