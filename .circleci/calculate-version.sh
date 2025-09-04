#!/bin/bash

############################################################################
## calculate-version.sh
## Intelligent version calculation for GitFlow branching strategy
## 
## This script determines the appropriate version based on:
## - Current branch name
## - Current version in pom.xml
## - GitFlow conventions
##
## Usage: ./calculate-version.sh [--dry-run]
## Output: Sets version in pom.xml and prints the new version
############################################################################

set -e

# Configuration
POM_FILE="pom.xml"
DRY_RUN=false

# Parse command line arguments
if [[ "$1" == "--dry-run" ]]; then
    DRY_RUN=true
    echo "DRY RUN MODE - No changes will be made"
fi

# Get current branch and version
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
CURRENT_VERSION=$(grep '<revision>' $POM_FILE | sed 's/.*<revision>//;s/<.*//')

echo "Current branch: $CURRENT_BRANCH"
echo "Current version: $CURRENT_VERSION"

# Function to extract version components
extract_version_parts() {
    local version=$1
    # Handle RC versions like 1.5.0-RC.1
    if [[ "$version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)-RC\.[0-9]+$ ]]; then
        MAJOR=${BASH_REMATCH[1]}
        MINOR=${BASH_REMATCH[2]}
        PATCH=${BASH_REMATCH[3]}
    # Handle SNAPSHOT versions like 1.5.0-SNAPSHOT
    elif [[ "$version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)-SNAPSHOT$ ]]; then
        MAJOR=${BASH_REMATCH[1]}
        MINOR=${BASH_REMATCH[2]}
        PATCH=${BASH_REMATCH[3]}
    # Handle stable versions like 1.5.0
    elif [[ "$version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
        MAJOR=${BASH_REMATCH[1]}
        MINOR=${BASH_REMATCH[2]}
        PATCH=${BASH_REMATCH[3]}
    else
        echo "ERROR: Cannot parse version format: $version"
        exit 1
    fi
}

# Function to calculate next version based on branch type
calculate_next_version() {
    local branch=$1
    
    case "$branch" in
        "develop")
            # Check if we just merged a release branch back to develop
            # Look for very specific patterns that indicate a release completion
            # Only look at recent commits to avoid historical merges triggering bumps
            RECENT_RELEASE_MERGES=$(git log --oneline -10 --grep="Merge.*release.*into.*develop" --grep="Merge.*release.*back.*develop" --grep="Bump.*version.*after.*release.*v" --since="3 days ago" || true)
            
            # Also check if current version suggests we're ready for next cycle
            if [[ -n "$RECENT_RELEASE_MERGES" ]] || [[ "$CURRENT_VERSION" =~ -RC\.[0-9]+$ ]] || [[ "$CURRENT_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
                # We just merged a release or have an RC/stable version, bump to next
                NEW_VERSION="$MAJOR.$((MINOR + 1)).0-SNAPSHOT"
            else
                # Keep current SNAPSHOT version - no recent release activity
                NEW_VERSION="$CURRENT_VERSION"
            fi
            ;;
            
        "main")
            # Main should always have stable versions, no changes needed
            NEW_VERSION="$CURRENT_VERSION"
            ;;
            
        release/*)
            # Extract major.minor from branch name (e.g., release/1.5 -> 1.5.0-RC.n)
            if [[ "$branch" =~ release/([0-9]+)\.([0-9]+) ]]; then
                BRANCH_MAJOR=${BASH_REMATCH[1]}
                BRANCH_MINOR=${BASH_REMATCH[2]}
                
                # Check if we already have an RC version
                if [[ "$CURRENT_VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)-RC\.([0-9]+)$ ]]; then
                    # Extract current RC number and increment it
                    CURRENT_RC=${BASH_REMATCH[4]}
                    NEW_RC=$((CURRENT_RC + 1))
                    NEW_VERSION="$BRANCH_MAJOR.$BRANCH_MINOR.0-RC.$NEW_RC"
                else
                    # First RC for this release
                    NEW_VERSION="$BRANCH_MAJOR.$BRANCH_MINOR.0-RC.1"
                fi
            else
                echo "ERROR: Invalid release branch format: $branch"
                exit 1
            fi
            ;;
            
        hotfix/*)
            # Bump patch version for hotfix
            NEW_VERSION="$MAJOR.$MINOR.$((PATCH + 1))"
            ;;
            
        feature/*|*)
            # Feature branches inherit version from develop, no changes
            NEW_VERSION="$CURRENT_VERSION"
            ;;
    esac
}

# Function to set version using Maven
set_version() {
    local new_version=$1
    
    if [[ "$DRY_RUN" == "true" ]]; then
        echo "DRY RUN: Would set version to: $new_version"
        echo "Command: mvn versions:set-property -Dproperty=revision -DnewVersion=$new_version -DgenerateBackupPoms=false"

        return
    fi
    
    echo "Setting version to: $new_version"
    
    # Use Maven versions plugin to set version
    mvn versions:set-property -Dproperty=revision -DnewVersion="$new_version" -DgenerateBackupPoms=false

    
    # Verify the change
    ACTUAL_VERSION=$(grep "<revision>" $POM_FILE | sed 's/.*<revision>//;s/<.*//')
    if [[ "$ACTUAL_VERSION" == "$new_version" ]]; then
        echo "✅ Version successfully updated to: $ACTUAL_VERSION"
    else
        echo "❌ Version update failed. Expected: $new_version, Got: $ACTUAL_VERSION"
        exit 1
    fi
}

# Main execution
main() {
    echo "=== QQQ Version Calculator ==="
    echo "Branch: $CURRENT_BRANCH"
    echo "Current version: $CURRENT_VERSION"
    echo ""
    
    # Extract version components
    extract_version_parts "$CURRENT_VERSION"
    echo "Version components: MAJOR=$MAJOR, MINOR=$MINOR, PATCH=$PATCH"
    echo ""
    
    # Calculate next version
    calculate_next_version "$CURRENT_BRANCH"
    echo "Calculated next version: $NEW_VERSION"
    echo ""
    
    # Set the version if it's different
    if [[ "$NEW_VERSION" != "$CURRENT_VERSION" ]]; then
        echo "Version change detected: $CURRENT_VERSION → $NEW_VERSION"
        set_version "$NEW_VERSION"
        
        # Show git diff
        echo ""
        echo "Changes made:"
        git diff $POM_FILE
    else
        echo "No version change needed. Current version is correct for branch: $CURRENT_BRANCH"
    fi
    
    echo ""
    echo "=== Version calculation complete ==="
}

# Run main function
main "$@"
