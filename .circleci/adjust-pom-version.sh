#!/bin/bash

## todo - some version of:
## - main (or -version tag?) noop?
## - dev ... replace -SNAPSHOT w/ -${timestamp}
## - other tags ... replace -SNAPSHOT w/ -${tag}-${timestamp}

POM=$(dirname $0)/../pom.xml
echo "On branch: $CIRCLE_BRANCH, tag: $CIRCLE_TAG..."

REVISION=$(grep '<revision>' $POM | sed 's/.*<revision>//;s/<.*//');
echo "<revision> in pom.xml is: $REVISION"
if [ \! $(echo "$REVISION" | grep SNAPSHOT) ]; then
   echo "Not on a SNAPSHOT revision, so nothing to do here."
   exit 0;
fi

SLUG=""
if [ $(echo "$CIRCLE_TAG" | grep ^snapshot-) ]; then
   SLUG=$(echo "$CIRCLE_TAG" | sed "s/^snapshot-//")-
   echo "Using slug [$SLUG] from tag [$CIRCLE_TAG]"

elif [ $(echo "$CIRCLE_BRANCH" |  grep ^integration/) ]; then
   SLUG=$(echo "$CIRCLE_BRANCH" | sed "s,/,-,g")-
   echo "Using slug [$SLUG] from branch [$CIRCLE_BRANCH]"
fi

TIMESTAMP=$(date +%Y%m%d-%H%M%S)
REPLACEMENT=${SLUG}${TIMESTAMP}

echo "Updating $POM -SNAPSHOT to: -$REPLACEMENT"
sed -i "s/-SNAPSHOT<\/revision>/-$REPLACEMENT<\/revision>/" $POM
git diff $POM

