#!/usr/bin/env bash
relativeToInvoke="$(dirname $0)"
baseDir="${relativeToInvoke}/.."

set -e
chmod +x ${baseDir}/test-env/src/main/resources/scripts/kafka/startDocker.sh

${baseDir}/test-env/src/main/resources/scripts/kafka/startDocker.sh
