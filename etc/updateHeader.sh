#!/bin/bash

# Updates the headers for all source files to match our current boilerplate

set -e

SRC_DIR=`dirname "$BASH_SOURCE"`/..

function updateOneFile() {
    file="$1"
    if [ -n "$file" ]; then
        echo "Updating $file"
        sed -i '/@since/d' "$file"
        sed -i '/^\/\/ End/d' "$file"
        sed -i'' '/(C) Copyright/,/\*\// {
                    /(C) Copyright/n
                    /\*\//r etc/header-boilerplate-tail.txt
                    d
                }' "$file"
    fi
}

function updateOneModule() {
    module="$1"
    find "$module" -name '*.java' -print0 | while IFS= read -r -d '' file; do updateOneFile "$file"; done
}

pushd $SRC_DIR
tail -n +3 etc/header-boilerplate.txt > etc/header-boilerplate-tail.txt
updateOneModule jgrapht-core
updateOneModule jgrapht-demo
updateOneModule jgrapht-ext
updateOneModule jgrapht-guava
updateOneModule jgrapht-io
updateOneModule jgrapht-opt
rm -f etc/header-boilerplate-tail.txt
popd
