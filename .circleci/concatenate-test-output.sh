#!/bin/bash

############################################################################
## Script to concatenate all .txt files in the surefire-reports directory
## into a single artifact that can be stored in CI.
############################################################################

mkdir -p /home/circleci/test-output-artifacts/

###################################################################
## Find all module directories that have target/surefire-reports ##
###################################################################
for module_dir in */; do
  if [ -d "${module_dir}target/surefire-reports" ]; then
    module_name=$(basename "${module_dir%/}")
    output_file="/home/circleci/test-output-artifacts/${module_name}-test-output.txt"
    
    echo "Processing module: ${module_name}"
    echo "Output file: ${output_file}"
    
    ##################################################################
    ## Concatenate all .txt files in the surefire-reports directory ##
    ##################################################################
    if [ -n "$(find "${module_dir}target/surefire-reports" -name "*.txt" -type f)" ]; then
      echo "=== Test Output for ${module_name} ===" > "${output_file}"
      echo "Generated at: $(date)" >> "${output_file}"
      echo "==========================================" >> "${output_file}"
      echo "" >> "${output_file}"
      
      ##############################################
      ## Sort files to ensure consistent ordering ##
      ##############################################
      find "${module_dir}target/surefire-reports" -name "*.txt" -type f | sort | while read -r txt_file; do
        echo "--- File: $(basename "${txt_file}") ---" >> "${output_file}"
        cat "${txt_file}" >> "${output_file}"
        echo "" >> "${output_file}"
        echo "--- End of $(basename "${txt_file}") ---" >> "${output_file}"
        echo "" >> "${output_file}"
        echo "" >> "${output_file}"
        echo "" >> "${output_file}"
      done
      
      echo "Concatenated test output for ${module_name} to ${output_file}"
    else
      echo "No .txt files found in ${module_dir}target/surefire-reports"
    fi
  fi
done 