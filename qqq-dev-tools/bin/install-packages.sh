#!/bin/bash

############################################################################
## install-packages.sh
## Used by the root-level install.sh - to ensure required packages are installed.
############################################################################

brew list coreutils     > /dev/null || brew install coreutils;
brew list gnu-sed       > /dev/null || brew install gnu-sed;
brew list jq            > /dev/null || brew install jq;
brew list gum           > /dev/null || (brew tap charmbracelet/tap && brew install charmbracelet/tap/gum);
brew list 1password-cli > /dev/null || brew install --cask 1password/tap/1password-cli
