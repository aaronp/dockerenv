#!/usr/bin/env bash

#
# This script exposes some functions which can be imported in order to start/stop/check we have a database (via
# a docker container) running.
#
# see https://hub.docker.com/_/mysql
export VOLUME_NAME=${VOLUME_NAME:-mysql-data}
export BACKUP_VOLUME_NAME=${VOLUME_NAME:-mysql-data-backup}
export IMAGE_NAME=${IMAGE_NAME:-dockerenvMysql}
export MYSQL_PORT=${MYSQL_PORT:-3306}

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

stopMySql () {
    docker stop "$IMAGE_NAME"
}

dockerRunMySql () {

    echo "Starting docker image $IMAGE_NAME, but first ensuring volume $VOLUME_NAME"

    ensureVolume

    export MYSQL_DATA_DIR=${MYSQL_DATA_DIR:-$(pwd)/data}
    mkdir -p "$MYSQL_DATA_DIR"
    echo "starting mysql w/ mysql_DATA_DIR set to $MYSQL_DATA_DIR"

# -v ${VOLUME_NAME}:/var/lib/mysql -v ${BACKUP_VOLUME_NAME}:/var/lib/mysql/backup
    MYSQL_CMD="docker run --rm --name $IMAGE_NAME -p 7777:$MYSQL_PORT -e MYSQL_ROOT_PASSWORD=docker  -d mysql:8.0.18"

    echo "Running '$MYSQL_CMD'"
    exec ${MYSQL_CMD}
}

# start up the docker mysql container if it's not already running
ensureRunning () {
  isRunning || dockerRunMySql
}

isRunning () {
  (docker ps | grep "$IMAGE_NAME") && echo "docker image "${IMAGE_NAME}" is running"
}
