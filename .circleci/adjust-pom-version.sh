#!/bin/bash

if [ -z "$CIRCLE_BRANCH" ]; then
   echo "Error:  env var CIRCLE_BRANCH was not set."
   exit 1;
fi

if [ "$CIRCLE_BRANCH" == "dev" ] || [ "$CIRCLE_BRANCH" == "staging" ] || [ "$CIRCLE_BRANCH" == "main" ]; then
   echo "On a primary branch [$CIRCLE_BRANCH] - will not edit the pom version.";
   exit 0;
fi

SLUG=$(echo $CIRCLE_BRANCH | sed 's/[^a-zA-Z0-9]/-/g')
POM=$(dirname $0)/../pom.xml

echo "Updating $POM <revision> to: $SLUG-SNAPSHOT"
sed -i "s/<revision>.*/<revision>$SLUG-SNAPSHOT<\/revision>/" $POM
git diff $POM
