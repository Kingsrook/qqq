#!/bin/bash

############################################################################
## Script to collect all JaCoCo reports from different modules into a
## single directory for easier artifact storage in CI.
############################################################################

mkdir -p /home/circleci/jacoco-reports/

##############################################################
## Find all module directories that have target/site/jacoco ##
##############################################################
for module_dir in */; do
  if [ -d "${module_dir}target/site/jacoco" ]; then
    module_name=$(basename "${module_dir%/}")
    target_dir="/home/circleci/jacoco-reports/${module_name}"
    
    echo "Collecting JaCoCo reports for module: ${module_name}"
    
    cp -r "${module_dir}target/site/jacoco" "${target_dir}"
    
    echo "Copied JaCoCo reports for ${module_name} to ${target_dir}"
  fi
done

echo "All JaCoCo reports collected to /home/circleci/jacoco-reports/" 