#!/usr/bin/env bash

source dockerFunctions.sh

docker exec -t ${IMAGE_NAME} /orientdb/bin/console.sh LIST DATABASES remote:localhost/test