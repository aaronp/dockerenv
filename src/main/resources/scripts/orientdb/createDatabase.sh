#!/usr/bin/env bash

source dockerFunctions.sh

docker exec -t ${IMAGE_NAME} /orientdb/bin/console.sh CREATE DATABASE remote:localhost/test root rootpwd PLOCAL
