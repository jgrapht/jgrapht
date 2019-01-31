#!/bin/bash

#Updates the year in our copyright statement. i.e. "* (C) Copyright 2003-2016," gets replaced by "* (C) Copyright 2003-[current_year],"

set -e

SRC_DIR=`dirname "$BASH_SOURCE"`/..

#get the current year
year=$(date +'%Y')

function updateOneFile() {
    file="$1"
    sed -i "s/\(\*\s(C)\sCopyright\s[0-9]\{4\}-\)[0-9]\{4\},/\1"$year",/" $file
}

function updateOneModule() {
    module="$1"
    find "$module" -name '*.java' -print0 | while IFS= read -r -d '' file; do updateOneFile "$file"; done
}

pushd $SRC_DIR
updateOneModule jgrapht-core
updateOneModule jgrapht-demo
updateOneModule jgrapht-ext
updateOneModule jgrapht-guava
updateOneModule jgrapht-io
updateOneModule jgrapht-opt
popd
