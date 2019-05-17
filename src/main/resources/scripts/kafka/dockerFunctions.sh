#!/usr/bin/env bash

#
# This script exposes some functions which can be imported in order to start/stop/check we have a our environment up.
#
# The scripts are a 'test' resource, as they are not mean to be used in production, but rather only via tests or as
# development tools.
#
# The scripts are invoked from code using 'KafkaEnv', which is then used by the 'BaseSpec' to ensure a database
# is running before each test.
#
# Alternatively the scripts (e.g. startDocker.sh, stopDocker.sh) can be used manually by developers if
# they're wanting to ensure an environment is running when starting up processes which run on their dev machine.
#

export VOLUME_NAME=${VOLUME_NAME:-kafka-data}
export CONTAINER_NAME=${CONTAINER_NAME:-test-kafka}
export IMAGE_NAME="dockerenv-kafka"

# see https://docs.docker.com/storage/volumes/
createVolume () {
	echo "Creating new volume $VOLUME_NAME"
	docker volume create "$VOLUME_NAME"
}

ensureVolume () {
  (docker volume ls | grep "$VOLUME_NAME") || createVolume
}

stopKafka() {
    docker stop "$CONTAINER_NAME"
}

buildImage() {
   echo "Building $IMAGE_NAME"
   (buildImage && echo "${IMAGE_NAME} built")
}

checkImageExists() {
   echo "checking if $IMAGE_NAME exists"
   (docker image ls | grep "${IMAGE_NAME}")
}

hasImage() {
    checkImageExists || buildImage
}
buildImage() {
    THIS_DIR="$(dirname ${0})"
    pushd ${THIS_DIR}
    echo "building $IMAGE_NAME in `pwd`"
    docker build . -t "${IMAGE_NAME}:test"
    popd
    if [[ ! checkImageExists ]]; then
      echo "checkImageExists still doesn't thing $IMAGE_NAME exists"
    else
      echo "checkImageExists is happy now"
    fi
}

dockerRunKafka() {
  hasImage && invokeDockerRunKafka
}

invokeDockerRunKafka() {
    THIS_DIR="$(dirname ${0})"
    export PROJECT_DIR=${PROJECT_DIR:-`pwd`}

    echo "THIS_DIR is ${THIS_DIR}"
    echo "PROJECT_DIR is ${PROJECT_DIR}"
    # see https://hub.docker.com/r/spotify/kafka and https://rmoff.net/2018/08/02/kafka-listeners-explained/ for exposing ADVERTISED_LISTENERS and ADVERTISED_HOST
    docker run --rm -t -d -h kafka0 --name "$CONTAINER_NAME" \
      -v ${PROJECT_DIR}:/project \
      -p 2181:2181 -p 9092:9092 -p 8083:8083 -p 9080:9080 \
      --env ADVERTISED_HOST=localhost \
      --env ADVERTISED_PORT=9092 \
      --env KAFKA_ADVERTISED_LISTENERS=LISTENER_BOB://kafka0:29092,LISTENER_FRED://localhost:9092 \
      --env KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=LISTENER_BOB:PLAINTEXT,LISTENER_FRED:PLAINTEXT \
      "${IMAGE_NAME}:test"

}

# start up the docker Kafkacontainer if it's not already running
ensureRunning () {
  isKafkaRunning || dockerRunKafka
}

isKafkaRunning () {
  (docker ps | grep "$CONTAINER_NAME") && echo "docker image "${CONTAINER_NAME}" is running"
}
