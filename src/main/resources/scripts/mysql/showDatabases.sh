#!/usr/bin/env bash

source dockerFunctions.sh

echo "IMAGE_NAME is $IMAGE_NAME"

docker exec -t ${IMAGE_NAME} mysql -u root --password=docker -e "SHOW DATABASES;"
