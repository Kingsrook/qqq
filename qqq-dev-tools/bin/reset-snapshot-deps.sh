#!/bin/bash

############################################################################
## reset-snapshot-deps.sh
## Reset the version of all qqq deps in a given pom to the current snapshot.
############################################################################

CURRENT_VERSION="$(cat $QQQ_DEV_TOOLS_DIR/CURRENT-SNAPSHOT-VERSION)"
MODULE_LIST_FILE=$QQQ_DEV_TOOLS_DIR/MODULE_LIST

for artifact in $(cat $MODULE_LIST_FILE); do
   update-dep.sh $artifact ${CURRENT_VERSION}-SNAPSHOT
done
