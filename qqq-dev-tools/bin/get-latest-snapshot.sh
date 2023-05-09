#!/bin/bash

############################################################################
## get-latest-snapshot.sh
## Queries the qqq maven registry for the current snapshot version of qqq modules.
############################################################################

CURRENT_VERSION="$(cat $QQQ_DEV_TOOLS_DIR/CURRENT-SNAPSHOT-VERSION)"
MODULE_LIST_FILE=$QQQ_DEV_TOOLS_DIR/MODULE_LIST
. $QQQ_DEV_TOOLS_DIR/.env

BRANCH=$(git rev-parse --abbrev-ref HEAD)
SLUG=$(echo $BRANCH | sed 's/[^a-zA-Z0-9]/-/g')

function checkForBranchBuild
{
   artifact=$1

   #############################################################
   ## on standard branches, don't look for branch deployments ##
   #############################################################
   if [ "$BRANCH" == "dev" ] || [ "$BRANCH" == "staging" ] || [ "$BRANCH" == "main" ]; then
      echo 0;
      return;
   fi

   ###################################################################
   ## else, do look for a branch deployment, and return accordingly ##
   ###################################################################
   curl -s --user ${GITHUB_USER}:${GITHUB_TOKEN} https://maven.pkg.github.com/Kingsrook/qqq-maven-registry/com/kingsrook/qqq/${artifact}/${SLUG}-SNAPSHOT/maven-metadata.xml | grep unable.to.fetch
   if [ "$?" == "1" ]; then
      echo 1;
      return;
   fi

   echo 0;
   return;
}

function getLatestVersion
{
   artifact=$1
   version=$2

   curl -s --user ${GITHUB_USER}:${GITHUB_TOKEN} https://maven.pkg.github.com/Kingsrook/qqq-maven-registry/com/kingsrook/qqq/${artifact}/${version}-SNAPSHOT/maven-metadata.xml > /tmp/metadata.xml
   ## xmllint -format - < /tmp/metadata.xml

   grep 'does not exist' /tmp/metadata.xml > /dev/null
   if [ "$?" == "0" ]; then
      echo "not found"
      return
   fi

   timestamp=$(xpath -q -e '/metadata/versioning/snapshot/timestamp/text()' /tmp/metadata.xml)
   buildNumber=$(xpath -q -e '/metadata/versioning/snapshot/buildNumber/text()' /tmp/metadata.xml)

   echo "$version-$timestamp-$buildNumber"
}

function promptForVersion
{
   echo "What version?"
   version=$(gum input --value $CURRENT_VERSION)
   if [ -z "$version" ]; then
      exit 1;
   fi
   echo " $version"
}

if [ "$1" == "-i" ]; then

   echo "What artifact?"
   artifact=$(cat $MODULE_LIST_FILE | gum filter)
   if [ -z "$artifact" ]; then
      exit 1;
   fi
   echo " $artifact"

   promptForVersion

   getLatestVersion $artifact $version

elif [ "$1" == "-a" ]; then

   version=$2
   if [ "$version" == "-l" ]; then
      version=$CURRENT_VERSION
   elif [ -z "$version" ]; then
      promptForVersion
   fi

   for artifact in $(cat $MODULE_LIST_FILE); do
      echo "$artifact $(getLatestVersion $artifact $version)"
   done

else

   artifact=$1
   version=$2
   if [ "$version" == "-l" ]; then
      useSlug=$(checkForBranchBuild $artifact)
      if [ "$useSlug" == "1" ]; then
         version=$SLUG
      else
         version=$CURRENT_VERSION
      fi
      echo "Using $version for $artifact" >&2
   fi

   if [ -z "$ar^tifact" -o -z "$version" ]; then
      echo "Usage: $0 artifact snapshot-version-prefix"
      echo "   or: $0 artifact -l (latest of CURRENT_VERSION, or branch-slug, if it has been deployed)"
      echo "   or: $0 -i (interactive mode)"
      echo "   or: $0 -a [snapshot-version-prefix] (all mode)"
      echo "Ex: $0 qqq-backend-core $CURRENT_VERSION"
      echo "Ex: $0 -i"
      echo "Ex: $0 -a"
      echo "Ex: $0 -a $CURRENT_VERSION"
      exit 1
   fi

   if [ -z "$version" ]; then
      promptForVersion
   fi

   getLatestVersion $artifact $version
fi


