#!/usr/bin/env bash

source dockerFunctions.sh
DBNAME="$1"
echo "CREATING $DBNAME"
docker exec -t ${IMAGE_NAME} mysql -u root --password=docker -e "CREATE DATABASE $DBNAME;"
