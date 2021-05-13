#!/usr/bin/env bash

#
# This script exposes some functions which can be imported in order to start/stop/check we have a database (via
# a docker container) running.
#
# see https://hub.docker.com/_/oracle
export VOLUME_NAME=${VOLUME_NAME:-oracle-data}
export BACKUP_VOLUME_NAME=${VOLUME_NAME:-oracle-data-backup}
export IMAGE_NAME=${IMAGE_NAME:-dockerenv-oracle}
export ORACLE_PORT=${ORACLE_PORT:-9020}

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

stopOracle () {
    docker stop "$IMAGE_NAME"
}

dockerRunOracle () {

    echo "Starting docker image $IMAGE_NAME, but first ensuring volume $VOLUME_NAME"

    ensureVolume

    export ORACLE_DATA_DIR=${ORACLE_DATA_DIR:-$(pwd)/data}
    mkdir -p "$ORACLE_DATA_DIR"
    echo "starting oracle w/ ORACLE_DATA_DIR set to $ORACLE_DATA_DIR"

    ORACLE_CMD="docker run --rm --name $IMAGE_NAME -p 5432:5432 -v $(pwd):/var/lib/oracleql/data -v ${VOLUME_NAME}:/oracle/databases -v ${BACKUP_VOLUME_NAME}:/oracle/backup -d oracleinanutshell/oracle-xe-11g:latest"

    echo "Running $ORACLE_CMD"
    exec ${ORACLE_CMD}
}

# start up the docker oracle container if it's not already running
ensureRunning () {
  isRunning || dockerRunOracle
}

isRunning () {
  (docker ps | grep "$IMAGE_NAME") && echo "docker image "${IMAGE_NAME}" is running"
}
