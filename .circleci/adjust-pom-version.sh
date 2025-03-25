#!/bin/bash

############################################################################
## adjust-pom.version.sh
## During CircleCI builds - edit the qqq parent pom.xml, to set the
## <revision> value such that:
## - feature-branch builds, tagged as snapshot-*, deploy with a version
##   number that includes that tag's name (minus the snapshot- part)
## - integration-branch builds deploy with a version number that includes
##   the branch name slugified
## - we never deploy -SNAPSHOT versions any more - because we don't believe
##   it is ever valid to not know exactly what versions you are getting
##   (perhaps because we are too loose with our versioning?)
############################################################################

POM=$(dirname $0)/../pom.xml
echo "On branch: $CIRCLE_BRANCH, tag: $CIRCLE_TAG..."

######################################################################
## ## only do anything if the committed pom has a -SNAPSHOT version ##
######################################################################
REVISION=$(grep '<revision>' $POM | sed 's/.*<revision>//;s/<.*//');
echo "<revision> in pom.xml is: $REVISION"
if [ \! $(echo "$REVISION" | grep SNAPSHOT) ]; then
   echo "Not on a SNAPSHOT revision, so nothing to do here."
   exit 0;
fi

##################################################################################
## ## figure out if we need a SLUG:  a snapshot- tag, or an integration branch ##
##################################################################################
SLUG=""
if [ $(echo "$CIRCLE_TAG" | grep ^snapshot-) ]; then
   SLUG=$(echo "$CIRCLE_TAG" | sed "s/^snapshot-//")-
   echo "Using slug [$SLUG] from tag [$CIRCLE_TAG]"

elif [ $(echo "$CIRCLE_BRANCH" |  grep ^integration) ]; then
   SLUG=$(echo "$CIRCLE_BRANCH" | sed "s,/,-,g")-
   echo "Using slug [$SLUG] from branch [$CIRCLE_BRANCH]"
fi

################################################################
## ## build the replcaement for -SNAPSHOT, and update the pom ##
################################################################
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
REPLACEMENT=${SLUG}${TIMESTAMP}

echo "Updating $POM -SNAPSHOT to: -$REPLACEMENT"
sed -i "s/-SNAPSHOT<\/revision>/-$REPLACEMENT<\/revision>/" $POM
git diff $POM

