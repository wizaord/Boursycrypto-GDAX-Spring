#!/bin/bash

__REAL_SCRIPTDIR=$( cd -P -- "$(dirname -- "$(command -v -- "$0")")" && pwd -P )
__REAL_PROJECTDIR=$(dirname ${__REAL_SCRIPTDIR})
__REAL_LOGDIR=${__REAL_PROJECTDIR}/logs
__JAR_NAME=$(find ${__REAL_PROJECTDIR} -name *jar)

[ ! -d ${__REAL_LOGDIR} ] && mkdir ${__REAL_LOGDIR} && echo "logs folder has been created"


echo "Running scripts LTC : ${__JAR_NAME}"
cd ${__REAL_PROJECTDIR}
nohup java -Dspring.profiles.active=PROD -Dspring.profiles.include=LTC -Dserver.port=10003 -jar ${__JAR_NAME} > ${__REAL_LOGDIR}/LTC.log &
