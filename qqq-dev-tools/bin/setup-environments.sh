#!/bin/bash

############################################################################
## setup-environments.sh
## Write .env files, pulling secrets from 1password
############################################################################

. $QQQ_DEV_TOOLS_DIR/lib/qqq-shell-functions.sh || (echo "QQQ Dev Tools not properly installed?"; read; exit)

function usage()
{
   echo "Usage: $0 [--nf-one|--qqq] [-q|--quiet]"
   echo "By default, all environments are set up.  Give an option to just do nf-one or qqq."
   exit 1;
}

DO_NF_ONE=0
DO_QQQ=0
QUIET=0

if [ -z "$1" ]; then
   DO_NF_ONE=1
   DO_QQQ=1
else
   for arg in ${@}; do
      if [ "$arg" == "--nf-one" ]; then
         DO_NF_ONE=1
      elif [ "$arg" == "--qqq" ]; then
         DO_QQQ=1
      elif [ "$arg" == "-q" -o "$arg" == "--quiet" ]; then
         QUIET=1
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
NUTRIFRESH_OP_LOCATION="op://NF-One-Development/"


##################################################
## repos which need environments setup for them ##
##################################################
QQQ_FRONTEND_MATERIAL_DASHBOARD_REPO_NAME="qqq-frontend-material-dashboard"
QQQ_PROJECT_NAME="qqq"
NF_ONE_REPO_NAME="Nutrifresh-One"

########################################################
## qqq modules which need environments setup for them ##
########################################################
QQQ_SAMPLE_PROJECT_MODULE_NAME="qqq-sample-project"
QQQ_BACKEND_CORE_MODULE_NAME="qqq-backend-core"
QQQ_BACKEND_MODULE_RDBMS_MODULE_NAME="qqq-backend-module-rdbms"
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
   $QQQ_BACKEND_CORE_MODULE_NAME
   $QQQ_DEV_TOOLS_MODULE_NAME
)



function createDotEnv
{
   repoName=$1
   repoLocation=$2
   isNutrifreshOneRepo=$3

   echo "${TAB}Changing directory to repository [$repoName] at [$repoLocation]..."
   cd "${repoLocation}" || exit

   if [ "$isNutrifreshOneRepo" != "1" ]; then

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

      echo "${TAB}Creating NF .env..."
      rm -rf .env
      op read "${NUTRIFRESH_OP_LOCATION}${repoName}/environment" > .env

      #####################################################################
      ## assume this is Nutrifresh One and copy down to the ui directory ##
      #####################################################################
      echo "${TAB}Copying .env to src/main/ui..."
      cd src/main/ui || exit
      rm -rf .env
      cp ../../../.env .

   fi
}



function setupRepoEnvironment
{
   repoName=$1
   isNutrifreshOneRepo=$2

   #############################################################
   ## try to automatically find the proper git repo directory ##
   #############################################################
   repoLocation=$(find ${HOME}/git -maxdepth 5 -type d -path "*${repoName}/.git" | sed 's/\/\.git//')

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

   createDotEnv ${repoName} ${repoLocation} $isNutrifreshOneRepo
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



##################
### NUTRIFRESH ###
##################
if [ "$DO_NF_ONE" == "1" ]; then

   ##################################################
   ## make sure signed into right 1passord account ##
   ##################################################
   echo
   echo "Signing in to Nutrifresh's 1password account..."
   op signin --account team-nutrifreshservices.1password.com
   setupRepoEnvironment ${NF_ONE_REPO_NAME} 1

fi


if [ "$QUIET" == "0" ]; then
   echo
   echo "All done!"
fi
