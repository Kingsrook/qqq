#!/bin/zsh

############################################################################
## update-all-qqq-deps.sh
## Set the version of all qqq deps in a given pom to the latest snapshot.
############################################################################

CURRENT_VERSION="$(cat $QQQ_DEV_TOOLS_DIR/CURRENT-SNAPSHOT-VERSION)"
MODULE_LIST_FILE=$QQQ_DEV_TOOLS_DIR/MODULE_LIST

for module in $(cat $MODULE_LIST_FILE); do
   version=$(get-latest-snapshot.sh $module $CURRENT_VERSION)
   update-dep.sh $module $version
done
