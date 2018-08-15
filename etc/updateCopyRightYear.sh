#!/bin/bash

#Updates the year in our copyright statement. i.e. "* (C) Copyright 2003-2016," gets replaced by "* (C) Copyright 2003-[current_year],"

#get the current year
year=$(date +'%Y')

find ../jgrapht-core/ -name *.java -print -exec sed -i "s/\(\*\s(C)\sCopyright\s[0-9]\{4\}-\)[0-9]\{4\},/\1"$year",/" {} \;
find ../jgrapht-demo/ -name *.java -print -exec sed -i "s/\(\*\s(C)\sCopyright\s[0-9]\{4\}-\)[0-9]\{4\},/\1"$year",/" {} \;
find ../jgrapht-dist/ -name *.java -print -exec sed -i "s/\(\*\s(C)\sCopyright\s[0-9]\{4\}-\)[0-9]\{4\},/\1"$year",/" {} \;
find ../jgrapht-ext/ -name *.java -print -exec sed -i "s/\(\*\s(C)\sCopyright\s[0-9]\{4\}-\)[0-9]\{4\},/\1"$year",/" {} \;
