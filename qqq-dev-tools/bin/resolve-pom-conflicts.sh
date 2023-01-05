#!/bin/bash

############################################################################
## resolve-pom-conflicts.sh
## Tries to automatically resove pom conflicts by putting SNAPSHOT back
############################################################################
gsed "/Updated upstream/,/=======/d" pom.xml | gsed "/Stashed/d" > /tmp/temp-pom.xml
mv /tmp/temp-pom.xml pom.xml
