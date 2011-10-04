#!/bin/sh -e -v

SCALAC=/usr/local/scala-2.9.1.final/bin/scalac

rm -f BehaviorSpace.jar
rm -rf build
mkdir -p build
find src -name \*.scala | xargs $SCALAC -d build -deprecation -encoding us-ascii -classpath ../../NetLogo.jar:scalatest_2.9.1-1.6.1.jar
jar cMf BehaviorSpace.jar -C build .
