#!/bin/bash

# Expands transclusions in markdown templates

set -e

shopt -s failglob

pushd ${TRAVIS_BUILD_DIR}/docs/guide-templates

for file in *.md; do
    hercule ${file} -o ${TRAVIS_BUILD_DIR}/docs/guide/EX-${file}
done

popd
