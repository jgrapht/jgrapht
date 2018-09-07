#!/bin/bash

#Applies code formatting rules defined by the eclipse formatter on all java classes recursively.

#Details about the formatter: http://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Ftasks%2Ftasks-231.htm
#Details about the formatter config file: http://help.eclipse.org/neon/topic/org.eclipse.jdt.doc.user/tasks/tasks-232.htm?cp=1_3_10_1

#Path to eclipse. Needs eclipse Neon or newer.
eclipse_path=/opt/eclipse/eclipse
#Path to Java
java_path=/usr/lib/jvm/java-8-oracle/bin/java
#
config_file=org.eclipse.jdt.core.prefs

find ../jgrapht-core/ -name *.java -print -exec $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file {} \;
find ../jgrapht-demo/ -name *.java -print -exec $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file {} \;
find ../jgrapht-dist/ -name *.java -print -exec $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file {} \;
find ../jgrapht-ext/ -name *.java -print -exec $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file {} \;
find ../jgrapht-guava/ -name *.java -print -exec $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file {} \;
find ../jgrapht-io/ -name *.java -print -exec $eclipse_path -nosplash -vm $java_path -application org.eclipse.jdt.core.JavaCodeFormatter -quiet -config $config_file {} \;
