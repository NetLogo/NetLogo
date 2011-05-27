#!/bin/sh -e -v

SCALAC=/usr/local/scala-2.9.0.1/bin/scalac

rm -f BehaviorSpace.jar
rm -rf build
mkdir -p build
find src -name \*.scala | xargs $SCALAC -d build -g -deprecation -encoding us-ascii -classpath ../../NetLogo.jar:../scalatest.jar
jar cMf BehaviorSpace.jar -C build .
