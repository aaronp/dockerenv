#!/usr/bin/env bash

script=$1
source dockerFunctions.sh

ensureRunning

docker exec "$IMAGE_NAME" bash -c "mongo < /data/mount/$script"
