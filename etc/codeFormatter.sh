#!/bin/bash

#Applies code formatting rules defined by the eclipse formatter on all java classes recursively.
#Details about the formatter: http://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Ftasks%2Ftasks-231.htm
#Details about the formatter config file: http://help.eclipse.org/neon/topic/org.eclipse.jdt.doc.user/tasks/tasks-232.htm?cp=1_3_10_1

#Path to eclipse. Needs eclipse Neon or newer.
eclipse_path=/opt/eclipse/eclipse
#Path to Java
java_path=/usr/lib/jvm/java-11-openjdk-amd64/bin/java
#format configuration
config_file=./etc/org.eclipse.jdt.core.prefs
#get the root dir (1st ancestor of the location where this script is stored)
SRC_DIR=`dirname "$BASH_SOURCE"`/..

set -e

function format(){
    find ./jgrapht-core/ -name *.java | parallel --no-notice --eta $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file
    
    find ./jgrapht-demo/ -name *.java | parallel --no-notice --eta $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file
    
    find ./jgrapht-dist/ -name *.java | parallel --no-notice --eta $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file

    find ./jgrapht-ext/ -name *.java | parallel --no-notice --eta $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file
    
    find ./jgrapht-guava/ -name *.java | parallel --no-notice --eta $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file
    
    find ./jgrapht-io/ -name *.java | parallel --no-notice --eta $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file
}

#switch to the root directory. This allows us to invoke this script from any directory. Then format.
pushd $SRC_DIR
format
popd
