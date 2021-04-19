#!/usr/bin/env bash

#
# This script exposes some functions which can be imported in order to start/stop/check we have a database (via
# a docker container) running.
#
# see https://hub.docker.com/_/mysql
export VOLUME_NAME=${VOLUME_NAME:-kafka-data}
export IMAGE_NAME=${IMAGE_NAME:-dockerenv-kafka-full}
export KAFKA_IMAGE=${KAFKA_IMAGE:-lensesio/fast-data-dev}

# see https://docs.docker.com/storage/volumes/
createVolume () {
	echo "Creating new volume $VOLUME_NAME"
	docker volume create "$VOLUME_NAME"
}

ensureVolume () {
  (docker volume ls | grep "$VOLUME_NAME") || createVolume
}

stopKafka () {
    docker stop "$IMAGE_NAME"
}

dockerRunKafka () {

    echo "Starting docker image $IMAGE_NAME, but first ensuring volume $VOLUME_NAME"

    ensureVolume

    DOCKER_CMD="docker run --net=host --rm --name $IMAGE_NAME -d \
       -p 2181:2181 \
       -p 3030:3030 \
       -p 8081-8083:8081-8083 \
       -p 9581-9585:9581-9585 \
       -p 9092:9092 \
        $KAFKA_IMAGE"

    echo "Running '$DOCKER_CMD'"
    exec ${DOCKER_CMD}
}

# start up the docker mysql container if it's not already running
ensureRunning () {
  isRunning || dockerRunKafka
}

isRunning () {
  (docker ps | grep "$IMAGE_NAME") && echo "docker image "${IMAGE_NAME}" is running"
}
