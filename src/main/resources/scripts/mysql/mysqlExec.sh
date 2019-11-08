#!/usr/bin/env bash

source dockerFunctions.sh
QUERY="$1"

docker exec -t ${IMAGE_NAME} mysql -u root --password=docker -e "$QUERY"
