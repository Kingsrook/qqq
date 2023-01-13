#!/bin/bash

############################################################################
## resolve-pom-conflicts.sh
## Tries to automatically resove pom conflicts by putting SNAPSHOT back
############################################################################
gsed -i "/Updated upstream/,/=======/d;/Stashed/d" pom.xml
