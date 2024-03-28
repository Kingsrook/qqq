#!/bin/bash

############################################################################
## xbar-circleci-latest.sh
## XBar script to give quick info on latest circleci jobs
## To use with xbar:
## - Install xbar from https://xbarapp.com/
## - create a symlink under $HOME/Library/Application Support/xbar/plugins/
##   pointed at this script - with a filename that indicates how frequently
##   you want it to run.  e.g., every-10-seconds (10s) as:
##     ln -s $QQQ_DEV_TOOLS_DIR/bin/xbar-circleci-latest.sh "$HOME/Library/Application Support/xbar/plugins/xbar-circleci-latest.10s.sh"
##   Then, in xbar, go to Plugin Browser, refresh, and :fingerscrossed:
############################################################################
. ~/.bashrc
. $QQQ_DEV_TOOLS_DIR/.env

DIR=/tmp/xbar-circleci-latest
mkdir -p $DIR
FILE=$DIR/cci.$$
JQ=/opt/homebrew/bin/jq
curl -s -H "Circle-Token: ${CIRCLE_TOKEN}" "https://circleci.com/api/v1.1/recent-builds?limit=10&shallow=true" > $FILE
NOW=$(date +%s)

needPipe=0

checkBuild()
{
   index=$1

   repo=$($JQ ".[$i].reponame" < $FILE | sed 's/"//g')
   branch=$($JQ ".[$i].branch" < $FILE | sed 's/"//g;s/null//;')
   tag=$($JQ ".[$i].vcs_tag" < $FILE | sed 's/"//g;s/null//;')
   buildStatus=$($JQ ".[$i].status" < $FILE | sed 's/"//g')
   url=$($JQ ".[$i].build_url" < $FILE | sed 's/"//g')
   jobName=$($JQ ".[$i].workflows.job_name" < $FILE | sed 's/"//g')
   avatarUrl=$($JQ ".[$i].user.avatar_url" < $FILE | sed 's/"//g')
   startDate=$($JQ ".[$i].queued_at" < $FILE | sed 's/"//g')
   if [ "$startDate" == "null" ]; then
      startDate=$($JQ ".[$i].committer_date" < $FILE | sed 's/"//g')
   fi
   endDate=$($JQ ".[$i].stop_time" < $FILE | sed 's/"//g;s/null//;')

   curl $avatarUrl > $DIR/avatar.jpg
   sips -s dpiHeight 96 -s dpiWidth 96 $DIR/avatar.jpg -o $DIR/avatar-96dpi.jpg > /dev/null
   sips -z 20 20 $DIR/avatar-96dpi.jpg -o $DIR/avatar-20.jpg > /dev/null
   base64 -i $DIR/avatar-20.jpg > $DIR/avatar.b64
   avatarB64=$(cat $DIR/avatar.b64)

   shortRepo="$repo"
   case $repo in
    qqq)                             shortRepo="qqq";;
    qqq-frontend-core)               shortRepo="fc";;
    qqq-frontend-material-dashboard) shortRepo="qfmd";;
    ColdTrack-Live)                  shortRepo="ctl";;
    ColdTrack-Live-Scripts)          shortRepo="cls";;
    Infoplus-Scripts)                shortRepo="ips";;
   esac

   timestamp=$(date -j -f "%Y-%m-%dT%H:%M:%S%z" $(echo "$startDate" | sed 's/\....Z/+0000/') +%s)
   seconds=$(( $NOW - $timestamp ))
   if [ $seconds -lt 120 ]; then
      age="$seconds seconds"
      shortAge="${seconds}s"
   elif [ $seconds -lt 7200 ]; then
      minutes=$(( $seconds / 60 ))
      age="$minutes minutes"
      shortAge="${minutes}m"
   elif [ $seconds -lt 172800 ]; then
      hours=$(( $seconds / 3600 ))
      age="$hours hours"
      shortAge="${hours}h"
   else
      days=$(( $seconds / 86400 ))
      age="$days days"
      shortAge="old"
   fi

   ## "status" : "failed", // :retried, :canceled, :infrastructure_fail, :timedout, :not_run, :running, :failed, :queued, :not_running, :no_tests, :fixed, :success
   if [ "$buildStatus" == "success" ]; then
      icon="✅"
      color="green"
   elif [ "$buildStatus" == "failed" ]; then
      icon="❌"
      color="red"
   elif [ "$buildStatus" == "running" ]; then
      ## icon="🏃"
      icon="⏱️"
      color="blue"
   else
      icon="❔"
      color="gray"
   fi


   ####################################################################################################
   ## if this is the 1st build, or it was less than some-short-time ago, then put it in the menu bar ##
   ####################################################################################################
   if [ $index -lt 1 -o $seconds -lt 600 ]; then

      ###########################################################################################
      ## put a pipe (unicode special pipe, to not break things) before all but the first build ##
      ###########################################################################################
      if [ "$needPipe" == "1" ]; then
         echo -n " ｜ "
      fi
      needPipe=1

      echo -n "${shortRepo}(${shortAge})${icon}"
   fi

   ########################################################
   ## always append the build to the dropdown ($details) ##
   ########################################################
   details="$details\n$repo/${branch}${tag}: $jobName: $buildStatus @ $age ago | color=$color | href=$url | image=$avatarB64"
}

details="---"
details="$details\n🔄 Refresh | refresh=true"

for i in $(seq 0 9); do
   checkBuild $i
done

## echo "@$(date +%M:%S)"
echo

echo -e "$details"

cp $FILE $DIR/cci-latest.json
rm $FILE
