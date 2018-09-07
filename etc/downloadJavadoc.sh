#!/bin/bash

# Downloads released Javadoc to local directory

set -e

pushd ${TRAVIS_BUILD_DIR}

rm -rf docs/javadoc*
git clone https://github.com/jgrapht/jgrapht-javadoc.git
mv jgrapht-javadoc/javadoc* docs
rm -rf jgrapht-javadoc

popd
