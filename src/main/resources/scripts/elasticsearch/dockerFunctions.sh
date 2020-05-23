#!/usr/bin/env bash

#
# This script exposes some functions which can be imported in order to start/stop/check we have a database (via
# a docker container) running.
#
# see https://hub.docker.com/_/mysql
export VOLUME_NAME=${VOLUME_NAME:-elasticsearch-data}
export IMAGE_NAME=${IMAGE_NAME:-dockerenv-elasticsearch}
export ES_IMAGE=${ES_IMAGE:-elasticsearch:7.5.1}

# see https://docs.docker.com/storage/volumes/
createVolume () {
	echo "Creating new volume $VOLUME_NAME"
	docker volume create "$VOLUME_NAME"
}

ensureVolume () {
  (docker volume ls | grep "$VOLUME_NAME") || createVolume
}

stopElastic () {
    docker stop "$IMAGE_NAME"
}

dockerRunElasticSearch () {

    echo "Starting docker image $IMAGE_NAME, but first ensuring volume $VOLUME_NAME"

    ensureVolume

    DOCKER_CMD="docker run --rm --name $IMAGE_NAME -d -p 9200:9200 -p 9300:9300 -e discovery.type=single-node $ES_IMAGE"

    echo "Running '$DOCKER_CMD'"
    exec ${DOCKER_CMD}
}

# start up the docker mysql container if it's not already running
ensureRunning () {
  isRunning || dockerRunElasticSearch
}

isRunning () {
  (docker ps | grep "$IMAGE_NAME") && echo "docker image "${IMAGE_NAME}" is running"
}
