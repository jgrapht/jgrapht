#!/bin/bash
# Prepare docs directory for deployment to gh-pages branch after build on master

set -e

: ${GITHUB_WORKSPACE?"variable value required"}

${GITHUB_WORKSPACE}/etc/expandMarkdown.sh
rm -f ${GITHUB_WORKSPACE}/docs/guide/.gitignore
${GITHUB_WORKSPACE}/etc/downloadJavadoc.sh
mv ${GITHUB_WORKSPACE}/target/site/apidocs ${GITHUB_WORKSPACE}/docs/javadoc-SNAPSHOT
