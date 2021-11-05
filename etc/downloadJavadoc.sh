#!/bin/bash

# Downloads released Javadoc to local directory

set -e

: ${GITHUB_WORKSPACE?"variable value required"}

pushd ${GITHUB_WORKSPACE}

rm -rf docs/javadoc*
git clone https://github.com/jgrapht/jgrapht-javadoc.git
mv jgrapht-javadoc/javadoc* docs
rm -rf jgrapht-javadoc

emit() {
    module=$1
    stripped=${2#"./"}
    file=${GITHUB_WORKSPACE}/docs/javadoc/$stripped
    mkdir -p $(dirname "$file")
    echo "---" > $file
    echo "redirect_to: " https://jgrapht.org/javadoc/$module/$stripped >> $file
    echo "---" >> $file
}

export -f emit

# Creates redirects from pre-module javadoc structure to post-module
pushd docs/javadoc
for dir in `ls -1 -d org.jgrapht.*`
do
    module=${dir%"/"}
    pushd ${module}
    find . -name '*.html' -print | xargs -I {} bash -c "emit ${module} {}"
    popd
done
popd

popd
