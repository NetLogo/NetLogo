#!/bin/sh

rlwrap java -classpath \
target/NetLogoHeadless.jar:\
$HOME/.sbt/boot/scala-2.10.1/lib/scala-library.jar:\
lib_managed/jars/asm/asm-all/asm-all-3.3.1.jar \
org.nlogo.headless.Shell "$@"
