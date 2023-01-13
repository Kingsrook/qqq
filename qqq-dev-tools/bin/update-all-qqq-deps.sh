#!/bin/zsh

############################################################################
## update-all-qqq-deps.sh
## Set the version of all qqq deps in a given pom to the latest snapshot.
############################################################################

if [ ! -e pom.xml ]; then
   echo "Error:  $0 Must be ran in a directory with a pom.xml"
   exit 1;
fi

CURRENT_VERSION="$(cat $QQQ_DEV_TOOLS_DIR/CURRENT-SNAPSHOT-VERSION)"
MODULE_LIST_FILE=$QQQ_DEV_TOOLS_DIR/MODULE_LIST

for module in $(cat $MODULE_LIST_FILE); do
   echo "Updating $module..."
   version=$(get-latest-snapshot.sh $module $CURRENT_VERSION)
   update-dep.sh $module $version -q
done

echo
echo git diff pom.xml
git diff pom.xml
newVersion=$(grep -A1 qqq-backend-core pom.xml | tail -1 | sed 's/.*<version>//;s/<\/version>.*//;s/-........\......./ snapshot/')
echo "You might want to commit that with:"
echo "   git commit -m \"Updated qqq deps to $newVersion\" pom.xml"
