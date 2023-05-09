#!/bin/bash

if [ -z "$CIRCLE_BRANCH" ] && [ -z "$CIRCLE_TAG" ]; then
   echo "Error:  env vars CIRCLE_BRANCH and CIRCLE_TAG were not set."
   exit 1;
fi

if [ "$CIRCLE_BRANCH" == "dev" ] || [ "$CIRCLE_BRANCH" == "staging" ] || [ "$CIRCLE_BRANCH" == "main" ]; then
   echo "On a primary branch [$CIRCLE_BRANCH] - will not edit the pom version.";
   exit 0;
fi

if [ -n "$CIRCLE_BRANCH" ]; then
   SLUG=$(echo $CIRCLE_BRANCH | sed 's/[^a-zA-Z0-9]/-/g')
else
   SLUG=$(echo $CIRCLE_TAG | sed 's/^snapshot-//g')
fi

POM=$(dirname $0)/../pom.xml

echo "Updating $POM <revision> to: $SLUG-SNAPSHOT"
sed -i "s/<revision>.*/<revision>$SLUG-SNAPSHOT<\/revision>/" $POM
git diff $POM
