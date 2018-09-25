#!/usr/bin/env bash

GREEN='\033[32m'
NO_COLOR='\033[0m' 

CMD="update_version"
SAMPLE="1.2.0-BETA-3 0.8.27 1.0.26 1.8.1"

if [ $# -ne 4 ]; then
    echo "-------------------------------------------------------------------------------"
    echo "Usage: $CMD <ReleaseVersion> <ramlParserV1Version> <ramlParserV2Version> <amfParserVersion>" 
    echo "  e.g: $CMD $SAMPLE"
    echo
    echo -e "$GREEN                                                               Powered by APIKit Team$NO_COLOR"
    echo "-------------------------------------------------------------------------------"
    exit 1
fi

releaseVersion="$1"
ramlParserV1Version="$2"
ramlParserV2Version="$3"
amfParserVersion="$4"

echo "Changing pom dependencies version ..."
./update_deps_versions.sh $ramlParserV1Version $ramlParserV2Version $amfParserVersion

echo "Changing version to $releaseVersion ..." 
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$releaseVersion


