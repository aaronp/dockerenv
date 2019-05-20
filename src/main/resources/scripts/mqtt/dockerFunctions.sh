#!/usr/bin/env bash

#
# This script exposes some functions which can be imported in order to start/stop/check we have a our environment up.
#
# The scripts are a 'test' resource, as they are not mean to be used in production, but rather only via tests or as
# development tools.
#
# The scripts are invoked from code using 'DockerEnv', which is then used by the 'BaseSpec' to ensure a database
# is running before each test.
#
# Alternatively the scripts (e.g. startDocker.sh, stopDocker.sh) can be used manually by developers if
# they're wanting to ensure an environment is running when starting up processes which run on their dev machine.
#

export VOLUME_NAME=${VOLUME_NAME:-mqtt-data}
export IMAGE_NAME=${IMAGE_NAME:-test-mqtt}

# see https://docs.docker.com/storage/volumes/
createVolume () {
	echo "Creating new volume $VOLUME_NAME"
	docker volume create "$VOLUME_NAME"
}

ensureVolume () {
  (docker volume ls | grep "$VOLUME_NAME") || createVolume
}

stopDocker() {
  docker stop "$IMAGE_NAME"
}

dockerRun() {
    echo "Starting docker image $IMAGE_NAME"

    docker run --rm -t -d -h mqtt0 --name "$IMAGE_NAME" -p 1883:1883 -p 9001:9001 eclipse-mosquitto
}

# start up the docker container if it's not already running
ensureRunning () {
  isRunning || dockerRun
}

isRunning () {
  (docker ps | grep "$IMAGE_NAME") && echo "docker image "${IMAGE_NAME}" is running"
}
