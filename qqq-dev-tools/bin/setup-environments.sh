#!/bin/bash

############################################################################
## setup-environments.sh
## Write .env files, pulling secrets from 1password
############################################################################

. $QQQ_DEV_TOOLS_DIR/lib/qqq-shell-functions.sh || (echo "QQQ Dev Tools not properly installed?"; read; exit)

function usage()
{
   echo "Usage: $0 [--ct-live|--qqq] [-q|--quiet] [-r|--is-for-release]"
   echo "By default, all environments are set up.  Give an option to just do ct-live or qqq."
   exit 1;
}

DO_CT_LIVE=0
DO_QQQ=0
QUIET=0
IS_FOR_RELEASE=0

if [ -z "$1" ]; then
   DO_CT_LIVE=1
   DO_QQQ=1
else
   for arg in ${@}; do
      if [ "$arg" == "--ct-live" -o "$arg" == "--ct-live" ]; then
         DO_CT_LIVE=1
      elif [ "$arg" == "--qqq" ]; then
         DO_QQQ=1
      elif [ "$arg" == "-q" -o "$arg" == "--quiet" ]; then
         QUIET=1
      elif [ "$arg" == "-r" -o "$arg" == "--is-for-release" ]; then
         IS_FOR_RELEASE=1
      else
         usage
      fi
   done
fi

TAB="   "

################
## silly logo ##
################
if [ "$QUIET" == "0" ]; then
   qqqLogo
fi


#########################################
## locations of env files in 1password ##
#########################################
QQQ_OP_LOCATION="op://Development Environments/"
CTL_OP_LOCATION="op://Engineering - CTL-Development/"


##################################################
## repos which need environments setup for them ##
##################################################
QQQ_FRONTEND_MATERIAL_DASHBOARD_REPO_NAME="qqq-frontend-material-dashboard"
QQQ_PROJECT_NAME="qqq"
CT_LIVE_REPO_NAME="ColdTrack-Live"

########################################################
## qqq modules which need environments setup for them ##
########################################################
QQQ_SAMPLE_PROJECT_MODULE_NAME="qqq-sample-project"
QQQ_BACKEND_CORE_MODULE_NAME="qqq-backend-core"
QQQ_BACKEND_MODULE_RDBMS_MODULE_NAME="qqq-backend-module-rdbms"
QQQ_BACKEND_MODULE_API_MODULE_NAME="qqq-backend-module-api"
QQQ_DEV_TOOLS_MODULE_NAME="qqq-dev-tools"


##############################
## make a list of QQQ repos ##
##############################
QQQ_REPO_LIST=(
   $QQQ_FRONTEND_MATERIAL_DASHBOARD_REPO_NAME
   $QQQ_PROJECT_NAME
)

#########################
## list of qqq modules ##
#########################
QQQ_MODULE_LIST=(
   $QQQ_SAMPLE_PROJECT_MODULE_NAME
   $QQQ_BACKEND_MODULE_RDBMS_MODULE_NAME
   $QQQ_BACKEND_MODULE_API_MODULE_NAME
   $QQQ_BACKEND_CORE_MODULE_NAME
   $QQQ_DEV_TOOLS_MODULE_NAME
)



function createDotEnv
{
   repoName=$1
   repoLocation=$2
   isColdTrackLiveRepo=$3

   echo "${TAB}Changing directory to repository [$repoName] at [$repoLocation]..."
   cd "${repoLocation}" || exit

   if [ "$isColdTrackLiveRepo" != "1" ]; then

      if [ "${repoName}" = "${QQQ_PROJECT_NAME}" ]; then

         for moduleName in "${QQQ_MODULE_LIST[@]}"
         do
            echo "${TAB}Creating QQQ .env for module [$moduleName]..."
            cd ${repoLocation}
            cd ${moduleName}
            rm -rf .env
            op read "${QQQ_OP_LOCATION}${moduleName}/environment" > .env
         done

      else

         echo "${TAB}Creating QQQ .env..."
         rm -rf .env
         op read "${QQQ_OP_LOCATION}${repoName}/environment" > .env

      fi

   else

      echo "${TAB}Creating CT-Live .env..."
      rm -rf .env
      op read "${CTL_OP_LOCATION}${repoName}/environment" > .env

   fi
}



function setupRepoEnvironment
{
   repoName=$1
   isColdTrackLiveRepo=$2

   repoSearchRoot=${HOME}/git
   if [ "$IS_FOR_RELEASE" == "1" ]; then
     repoSearchRoot="/tmp/qqq-release"
   fi

   #############################################################
   ## try to automatically find the proper git repo directory ##
   #############################################################
   repoLocation=$(find ${repoSearchRoot} -maxdepth 5 -type d -path "*${repoName}/.git" | sed 's/\/\.git//')

   if [ "$repoLocation" != "" ]; then

      #########################################
      ## if found confirm that it is correct ##
      #########################################
      echo
      echo "Found repository [$repoName] under directory [$repoLocation]. "
      echo "If correct, press enter to continue, otherwise enter a different repository location: "
      read inputLocation
      if [ "$inputLocation" != "" ]; then
         repoLocation="$inputLocation"
      fi

   else

      echo
      echo "Could not find a directory containing repository [$repoName]."
      echo "Enter the directory containing this repo: "
      read inputLocation
      repoLocation="$inputLocation"

   fi

   #############################
   ## remove trailing slashes ##
   #############################
   repoLocation=$(echo $repoLocation | sed 's/\/*$//')

   ########################################################
   ## confirm the directory exists and is a git checkout ##
   ########################################################
   if [ ! -d "${repoLocation}/.git" ]; then
      echo
      echo "Invalid git directory was given, quitting..." && exit 1
   fi

   createDotEnv ${repoName} ${repoLocation} $isColdTrackLiveRepo
}



###########
### QQQ ###
###########
if [ "$DO_QQQ" == "1" ]; then

   ##################################################
   ## make sure signed into right 1passord account ##
   ##################################################
   echo
   echo "Signing in to Kingsrook's 1password account..."
   op signin --account kingsrook.1password.com

   ######################################
   ## build list of all qqq repo names ##
   ######################################
   for repoName in "${QQQ_REPO_LIST[@]}"
   do
      setupRepoEnvironment $repoName
   done

fi



#################
### COLDTRACK ###
#################
if [ "$DO_CT_LIVE" == "1" ]; then

   ###################################################
   ## make sure signed into right 1password account ##
   ###################################################
   echo
   echo "Signing in to ColdTrack's 1password account..."
   op signin --account coldtrack.1password.com
   setupRepoEnvironment ${CT_LIVE_REPO_NAME} 1

fi


if [ "$QUIET" == "0" ]; then
   echo
   echo "All done!"
fi
