#!/bin/sh
# Updates a Mule dependency version in a branch and push the commit.

set -o errexit
WORK_DIRECTORY=APIKIT
REPOSITORY=git@github.com:mulesoft/apikit.git
BRANCH=$1
DEPENDENCY=$2
VERSION=$3

if [ -z "$3" ]; then
  echo "";
  echo "Usage: \033[1mupdateDependenciesVersion.sh\033[0m <branch> <dependency> <version>";
  echo "";
  echo "Updates dependency version in the branch (use the branch number without the mule prefix. Example 3.6.1 for the branch mule-3.6.1)."
  echo "To update to the current Apikit version use version=project.version";
  echo "";
  echo "Examples:";
  echo "";
  echo "updateDependenciesVersion.sh 3.6.x DataMapper 3.6.2-SNAPSHOT";
  echo "";
  echo "updateDependenciesVersion.sh 3.x AES 3.6.2-SNAPSHOT";
  echo "";
  exit 1;
fi


log() {
    echo "[INFO] $1"
}

updateDependenciesVersion() {
  REPOSITORY=$1
  WORK_DIRECTORY=$2

  log "Updating in $WORK_DIRECTORY $DEPENDENCY on branch $BRANCH to $VERSION..."
  rm -rf $WORK_DIRECTORY
  git clone --depth 1 --branch $BRANCH $REPOSITORY $WORK_DIRECTORY
  cd $WORK_DIRECTORY
  updateDependencies $WORK_DIRECTORY
  cd ..
}

pushChanges() {
  log "Pushing branch $BRANCH to $WORK_DIRECTORY with $DEPENDENCY new version $VERSION"
  git add .
  git commit -m "$DEPENDENCY version to $VERSION"
  git push -v origin $BRANCH
}

updateDependencies() {
  log "Updating $DEPENDENCY version in local $WORK_DIRECTORY repository."

  DEPENDENCY_AUX=$DEPENDENCY

  if [[ $VERSION = "project.version" ]]; then
    groovy ../$WORK_DIRECTORY/buildtools/scripts/UpdateMuleSoftDependencies.groovy -d $DEPENDENCY_AUX -p
  else
    groovy ../$WORK_DIRECTORY/buildtools/scripts/UpdateMuleSoftDependencies.groovy -d $DEPENDENCY_AUX -t $VERSION
  fi
  status=$(git status --porcelain)
  if [ -z "$status" ] ; then
	  log "Found no changes after updating version, skipping push"
  else
      pushChanges
  fi;
}

set -o nounset

updateDependenciesVersion $REPOSITORY $WORK_DIRECTORY
