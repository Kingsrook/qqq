#!/bin/bash

DIR=$(dirname $0)
. ${DIR}/../lib/quickLoop.sh


defineOption 1 "ls"    "Short listing"
defineOption 2 "ls -l" "Long listing"
defineOption 3 "tree"  "Tree listing"

quickLoop;
