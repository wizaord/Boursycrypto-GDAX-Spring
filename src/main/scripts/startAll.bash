#!/bin/bash

__REAL_SCRIPTDIR=$( cd -P -- "$(dirname -- "$(command -v -- "$0")")" && pwd -P )

${__REAL_SCRIPTDIR}/runBTC.bash
${__REAL_SCRIPTDIR}/runETH.bash
${__REAL_SCRIPTDIR}/runLTC.bash
${__REAL_SCRIPTDIR}/runBCH.bash

