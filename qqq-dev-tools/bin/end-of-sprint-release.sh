#!/bin/bash

############################################################################
## Script to run the release process on qqq at the end of a sprint.
## It's a good idea to run it from a fresh clone, but that's not strictly needed.
## Uses gum for CLI UI.
############################################################################

function gumBanner
{
   gum style --foreground "#60A0FF" --border thick --width 80 --align=center "${@}"
}

function gumConfirmProceed
{
   prompt=$1
   affirmative=$2
   negative=$3
   gum confirm "$prompt" --prompt.border thick --prompt.width 80 --prompt.align center --affirmative="$affirmative" --negative="$negative"

   if [ "$?" == "1" ]; then
      echo "OK, exiting."
      exit;
   fi
}

gumBanner "Making sure you have a clean git checkout"
git status
gumConfirmProceed "Can we Proceed, or do you need to clean up your checkout (git stash -u)?" "Proceed" "I need to clean up my checkout"

gumBanner "Checking for open PR's..."
gh pr list
gumConfirmProceed "Can we Proceed, or are there open PR's that need merged?" "Proceed" "There are open PR's that need merged"

gumBanner "Getting dev & main branches up to date and ready"
git checkout main && git pull && git checkout dev && git pull

if [ ! -e "qqq-sample-project/.env" ]; then
   dir=$(realpath .)
   gumBanner "Installing .env file -- for qqq" "Tell it your qqq is at:" "$dir"
   setup-environments.sh --qqq --quiet
fi

###################################
## go back to root qqq directory ##
###################################
cd ~/git/kingsrook/qqq/

MVN_VERIFY_LOG=/tmp/mvn-verify.log
gumBanner "Doing clean build (logging to $MVN_VERIFY_LOG)"
cp ~/git/kingsrook/qqq/qqq-sample-project/.env qqq-sample-project
mvn clean verify > $MVN_VERIFY_LOG 2>&1
tail -30 $MVN_VERIFY_LOG
gumConfirmProceed "Can we Proceed, or are there build errors to fix?" "Proceed" "There are build errors to fix"

gumBanner "Running mvn gitflow:release-start"
mvn gitflow:release-start
gumConfirmProceed "Can we Proceed, or were there errors from the gitflow:release-start?" "Proceed" "There were errors..."

gumBanner "Pushining release branch to origin"
git push --set-upstream origin "$(git rev-parse --abbrev-ref HEAD)"

gumBanner "Please wait for a green run in CI on the release branch..."
gumConfirmProceed "Can we Proceed, or did CI not pass on the release branch?" "Proceed" "CI did not pass..."

gumBanner "Running mvn gitflow:release-finish"
mvn gitflow:release-finish

gumBanner "Updating qqq-dev-tools/CURRENT-SNAPSHOT-VERSION"
CURRENT_SNAPSHOT_VERSION=$(grep '<revision>' pom.xml | sed 's/<.\?revision>//g;s/-SNAPSHOT//;s/ //g;')
echo $CURRENT_SNAPSHOT_VERSION > $QQQ_DEV_TOOLS_DIR/CURRENT-SNAPSHOT-VERSION
cd $QQQ_DEV_TOOLS_DIR
git commit -m "Updating to $CURRENT_SNAPSHOT_VERSION" CURRENT-SNAPSHOT-VERSION
git push

gumBanner "Done!"
echo "(My notes say to delete branches and snapshot tags now... and to update dependent projects)"
