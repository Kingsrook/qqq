#!/bin/zsh

############################################################################
## xbar-latest-snapshots.sh
## XBar script to give quick access to current qqq module snapshot build versions
## To use with xbar:
## - Install xbar from https://xbarapp.com/
## - create a symlink under $HOME/Library/Application Support/xbar/plugins/
##   pointed at this script - with a filename that indicates how frequently
##   you want it to run.  e.g., every-60-seconds (60s) as:
##     ln -s $QQQ_DEV_TOOLS_DIR/bin/xbar-latest-snapshots.sh "$HOME/Library/Application Support/xbar/plugins/xbar-latest-snapshots.60s.sh
##   Then, in xbar, go to Plugin Browser, refresh, and :fingerscrossed:
############################################################################

echo "Versions@$(date +%M:%S)"
echo "---"
echo "🔄 Refresh | refresh=true"
DIR=$(realpath $(dirname $0))

. ~/.zshrc

function doOne
{
   name=$1
   version=$($QQQ_DEV_TOOLS_DIR/bin/get-latest-snapshot.sh $name -l)

   rest="| font=Menlo"
   echo "$name $version" | sed 's/ \(.*\)/ \1 | shell="bash" param1="-c" param2="echo \1 | pbcopy"/' | sed "s/\$/ $rest/"
}

doOne "qqq-backend-core              "
doOne "qqq-backend-module-rdbms      "
doOne "qqq-backend-module-filesystem "
doOne "qqq-backend-module-api        "
doOne "qqq-middleware-picocli        "
doOne "qqq-middleware-javalin        "
