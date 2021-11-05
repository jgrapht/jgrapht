#!/bin/bash

# Expands transclusions in markdown templates
#
# Usage:
#
#    export GITHUB_WORKSPACE=/path/to/jgrapht-clone
#    expandMarkdown.sh [ github-user-id/repository-name/branch-name ]
#
# If the argument is omitted, then jgrapht/jgrapht/master is implicit.

set -e

: ${GITHUB_WORKSPACE?"variable value required"}

shopt -s failglob

USER_BRANCH=${1:-jgrapht/jgrapht/master}

pushd ${GITHUB_WORKSPACE}/docs/guide-templates

for file in *.md; do
    outfile="${GITHUB_WORKSPACE}/docs/guide/${file}"
    rm -f ${outfile}
    echo "Expanding ${file} to ${outfile}"
    sed -e "s#raw/master#raw/user/${USER_BRANCH}#g" < ${file} | \
        hercule --stdin -o ${outfile}
done

popd
