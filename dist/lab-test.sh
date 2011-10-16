#!/bin/sh -e -v

java -classpath BehaviorSpace.jar:../../NetLogo.jar:scalatest_2.9.1-1.6.1.jar org.scalatest.tools.Runner -p build
