#!/bin/bash

############################################################################
## install.sh
## This script installs the qqq-dev-tools onto a user's mac.
## e.g., by adding required brew packages, and settings in ~/zshrc
############################################################################

function introBanner()
{
   gum style --border double --margin 1 --padding 1 --align center --width 80 --foreground "#FFFF00" --border-foreground "#0000B0" \
      "Installing qqq-dev-tools at:" \
      $QQQ_DEV_TOOLS_DIR
}

##################################################
## check if gum and realpath are both available ##
##################################################
which gum && which realpath > /dev/null 2>&1
if [ "$?" == "0" ]; then
   ###################################################################################
   ## if so, use realpath to get the QQQ_DEV_TOOLS_DIR                              ##
   ## print our gum banner, and use a gum spinner while checking for other packages ##
   ###################################################################################
   QQQ_DEV_TOOLS_DIR=$(dirname $(realpath $0))
   introBanner
   gum spin --title "Checking for / Installing required homebrew packages..." -- $QQQ_DEV_TOOLS_DIR/bin/install-packages.sh
else
   ##################################################################################################
   ## if not, run the install-packages script just based dirname of $0, and don't give gum spinner ##
   ## after packages are installed, we can use realpath to get the actual QQQ_DEV_TOOLS_DIR        ##
   ## then print our gum introBanner and resume                                                    ##
   ##################################################################################################
   DIR=$(dirname $0)
   echo "Checking for / Installing required homebrew packages..."
   $DIR/bin/install-packages.sh
   QQQ_DEV_TOOLS_DIR=$(dirname $(realpath $0))
   introBanner
fi

function addVarsToRc()
{
   file=$1
   grep '##QQQ_DEV_TOOLS:start' $file > /dev/null
   if [ "$?" == "0" ]; then
      echo
      echo "Found existing QQQ_DEV_TOOLS section in $file:"
      echo
      gsed '/##QQQ_DEV_TOOLS:start/,/##QQQ_DEV_TOOLS:end/p;d' $file | gum format -t code
      echo
      gum confirm "Please confirm removal (for replacement) of these lines of $file"
      if [ "$?" == "1" ]; then
         echo "OK, exiting."
         exit;
      fi
      gsed -i '/##QQQ_DEV_TOOLS:start/,/##QQQ_DEV_TOOLS:end/d' $file
   fi

   echo "Adding QQQ_DEV_TOOLS section to $file"
   cat <<EOF >> $file
##QQQ_DEV_TOOLS:start
## This section of this file was written automatically by:
##   $(realpath $0).
## It may be re-written in the future by re-running that script.
## Unless, that is, you edit things here. Then, you're on your own :)
export QQQ_DEV_TOOLS_DIR=$QQQ_DEV_TOOLS_DIR
export PATH="$QQQ_DEV_TOOLS_DIR/bin:\$PATH"
. $QQQ_DEV_TOOLS_DIR/lib/qqq-shell-functions.sh
##QQQ_DEV_TOOLS:end
EOF
}

addVarsToRc ~/.zshrc
addVarsToRc ~/.bashrc

tada=$(echo ':tada:' | gum format --type emoji)
gum style --border double --margin 1 --padding 1 --align center --width 80 --foreground "#00FF00" --border-foreground "#0000B0" \
   "QQQ Dev Tools installation is complete $tada" \
   "" \
   "You'll want to either re-source ~/.zshrc, or make a new terminal session, to start using it."
