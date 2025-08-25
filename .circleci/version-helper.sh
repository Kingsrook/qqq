#!/bin/bash

############################################################################
## version-helper.sh
## Helper script for managing versions according to GitFlow strategy.
## This script helps with explicit version management but does NOT
## automatically change versions during CI builds.
##
## Usage:
##   ./version-helper.sh set-rc 1.5.0 1      # Sets version to 1.5.0-RC.1
##   ./version-helper.sh set-release 1.5.0   # Sets version to 1.5.0
##   ./version-helper.sh set-snapshot 1.6.0  # Sets version to 1.6.0-SNAPSHOT
##   ./version-helper.sh current             # Shows current version
############################################################################

set -e

POM=$(dirname $0)/../pom.xml

get_current_version() {
    grep '<revision>' $POM | sed 's/.*<revision>//;s/<.*//;'
}

set_version() {
    local new_version=$1
    echo "Setting version to: $new_version"
    sed -i.bak "s/<revision>.*<\/revision>/<revision>$new_version<\/revision>/" $POM
    rm $POM.bak
    echo "Updated $POM"
    git diff $POM
}

case "${1:-}" in
    "current")
        echo "Current version: $(get_current_version)"
        ;;
    "set-rc")
        if [[ $# -ne 3 ]]; then
            echo "Usage: $0 set-rc <major.minor> <rc-number>"
            echo "Example: $0 set-rc 1.5.0 1"
            exit 1
        fi
        set_version "$2-RC.$3"
        ;;
    "set-release")
        if [[ $# -ne 2 ]]; then
            echo "Usage: $0 set-release <version>"
            echo "Example: $0 set-release 1.5.0"
            exit 1
        fi
        set_version "$2"
        ;;
    "set-snapshot")
        if [[ $# -ne 2 ]]; then
            echo "Usage: $0 set-snapshot <version>"
            echo "Example: $0 set-snapshot 1.6.0"
            exit 1
        fi
        set_version "$2-SNAPSHOT"
        ;;
    *)
        echo "QQQ Version Helper"
        echo ""
        echo "Usage: $0 <command> [args...]"
        echo ""
        echo "Commands:"
        echo "  current                     Show current version"
        echo "  set-rc <major.minor> <num>  Set release candidate version"
        echo "  set-release <version>       Set release version"
        echo "  set-snapshot <version>      Set snapshot version"
        echo ""
        echo "Examples:"
        echo "  $0 current"
        echo "  $0 set-rc 1.5.0 1"
        echo "  $0 set-release 1.5.0"
        echo "  $0 set-snapshot 1.6.0"
        exit 1
        ;;
esac