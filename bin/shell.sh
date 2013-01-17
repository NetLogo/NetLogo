#!/bin/sh

rlwrap java -classpath \
headless/target/NetLogoHeadless.jar:\
$HOME/.sbt/boot/scala-2.10.0/lib/scala-library.jar:\
lib_managed/jars/asm/asm-all/asm-all-3.3.1.jar:\
lib_managed/bundles/log4j/log4j/log4j-1.2.17.jar:\
lib_managed/jars/org.picocontainer/picocontainer/picocontainer-2.13.6.jar \
org.nlogo.headless.Shell "$@"
