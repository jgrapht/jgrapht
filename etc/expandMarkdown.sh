#!/bin/bash

# Expands transclusions in markdown templates
#
# Usage:
#
#    export TRAVIS_BUILD_DIR=/path/to/jgrapht-clone
#    expandMarkdown.sh [ github-user-id/repository-name/branch-name ]
#
# If the argument is omitted, then jgrapht/jgrapht/master is implicit.

set -e

: ${TRAVIS_BUILD_DIR?"variable value required"}

shopt -s failglob

USER_BRANCH=${1:-jgrapht/jgrapht/master}

pushd ${TRAVIS_BUILD_DIR}/docs/guide-templates

for file in *.md; do
    outfile="${TRAVIS_BUILD_DIR}/docs/guide/${file}"
    rm -f ${outfile}
    echo "Expanding ${file} to ${outfile}"
    sed -e "s#raw/master#raw/user/${USER_BRANCH}#g" < ${file} | \
        hercule --stdin -o ${outfile}
done

popd
