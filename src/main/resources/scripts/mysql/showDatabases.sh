#!/usr/bin/env bash

source dockerFunctions.sh

docker exec -t ${IMAGE_NAME} mysql -u root --password=docker -e "SHOW DATABASES;"
