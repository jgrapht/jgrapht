#!/bin/bash
# Prepare docs directory for deployment to gh-pages branch after build on master

: ${TRAVIS_BUILD_DIR?"variable value required"}

${TRAVIS_BUILD_DIR}/etc/expandMarkdown.sh
rm -f ${TRAVIS_BUILD_DIR}/docs/guide/.gitignore
${TRAVIS_BUILD_DIR}/etc/downloadJavadoc.sh
mv ${TRAVIS_BUILD_DIR}/target/site/apidocs ${TRAVIS_BUILD_DIR}/docs/javadoc-SNAPSHOT
