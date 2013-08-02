#!/bin/sh

if [ -z "$JAVA_HOME" ]
then
  JAVA=java
else
  JAVA=$JAVA_HOME/bin/java
fi

if [ ! -f "target/NetLogoHeadless.jar" ]; then
echo "missing target/NetLogoHeadless.jar; run 'sbt package'"
exit 1
fi

rlwrap $JAVA -classpath \
target/NetLogoHeadless.jar:\
$HOME/.sbt/0.13.0-RC4/boot/scala-2.10.2/lib/scala-library.jar:\
lib_managed/jars/asm/asm-all/asm-all-3.3.1.jar \
org.nlogo.headless.Shell "$@"
