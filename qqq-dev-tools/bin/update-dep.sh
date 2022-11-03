#!/bin/bash

############################################################################
## update-dep.sh
## Update one qqq dependency in a pom with a specific snapshot version
############################################################################

dep=$1
version=$2

verbose=1
if [ "$3" == "-q" ]; then
   verbose=0;
fi

if [ -z "$dep" -o -z "$version" ]; then

   echo "What dependency?"
   dep=$(cat $QQQ_DEV_TOOLS_DIR/MODULE_LIST | gum filter)
   if [ -z "$dep" ]; then
      exit 1;
   fi
   echo " $dep"

   echo "What version?"
   version=$(gum input --placeholder 0.0.0-20220202.202022-1)
   if [ -z "$version" ]; then
      exit 1;
   fi
   echo " $version"
fi

lines=$(grep -n "<artifactId>.*$dep" pom.xml)
if [ $? != 0 -o $(echo "$lines" | wc -l) -ne 1 ]; then
   echo "Error:  couldn't really tell where $dep was in the pom.xml"
   exit 1;
fi
lineNo=$(( $(echo $lines | cut -d: -f1) + 1 ))

dependenciesTagLineNo=$(grep -n "<dependencies>" pom.xml | head -1 | cut -d: -f1)
if [ $lineNo -lt $dependenciesTagLineNo ]; then
   echo "Not updating $dep in what appears to be the pom for $dep."
   exit 0;
fi

if [ "$verbose" == "1" ]; then
   echo "Going to update version of $dep at line $lineNo"
fi

gsed -i "${lineNo}s/<version>.*</<version>$version</" pom.xml

if [ "$verbose" == "1" ]; then
   git diff pom.xml
fi
