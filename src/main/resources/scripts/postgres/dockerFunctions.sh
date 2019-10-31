#!/usr/bin/env bash

#
# This script exposes some functions which can be imported in order to start/stop/check we have a database (via
# a docker container) running.
#
# see https://hub.docker.com/_/postgres
export VOLUME_NAME=${VOLUME_NAME:-postgres-data}
export BACKUP_VOLUME_NAME=${VOLUME_NAME:-postgres-data-backup}
export IMAGE_NAME=${IMAGE_NAME:-dockerenv-postgres}
export POSTGRES_PORT=${POSTGRES_PORT:-9020}

# see https://docs.docker.com/storage/volumes/
createVolume () {
	echo "Creating new volume $VOLUME_NAME"
	docker volume create "$VOLUME_NAME"
}
createBackupVolume () {
	echo "Creating new backup volume $BACKUP_VOLUME_NAME"
	docker volume create "$BACKUP_VOLUME_NAME"
}

ensureVolume () {
  (docker volume ls | grep "$VOLUME_NAME") || createVolume
  (docker volume ls | grep "$BACKUP_VOLUME_NAME") || createBackupVolume
}

stopPostgres () {
    docker stop "$IMAGE_NAME"
}

dockerRunPostgres () {

    echo "Starting docker image $IMAGE_NAME, but first ensuring volume $VOLUME_NAME"

    ensureVolume

    export POSTGRES_DATA_DIR=${POSTGRES_DATA_DIR:-$(pwd)/data}
    mkdir -p "$POSTGRES_DATA_DIR"
    echo "starting postgres w/ postgres_DATA_DIR set to $POSTGRES_DATA_DIR"

    POSTGRES_CMD="docker run --rm --name $IMAGE_NAME -p 5432:5432 -e POSTGRES_PASSWORD=docker -v $(pwd):/var/lib/postgresql/data -v ${VOLUME_NAME}:/postgres/databases -v ${BACKUP_VOLUME_NAME}:/postgres/backup -d postgres"

    echo "Running $POSTGRES_CMD"
    exec ${POSTGRES_CMD}
}

# start up the docker postgres container if it's not already running
ensureRunning () {
  isRunning || dockerRunPostgres
}

isRunning () {
  (docker ps | grep "$IMAGE_NAME") && echo "docker image "${IMAGE_NAME}" is running"
}
