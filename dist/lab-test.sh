#!/bin/sh -e -v

JAVA='java -Djava.awt.headless=true'

$JAVA -classpath BehaviorSpace.jar:../../NetLogo.jar:../scalatest.jar ScalaTest org.nlogo.lab
$JAVA -classpath BehaviorSpace.jar:../../NetLogo.jar:../scalatest.jar ScalaTest org.nlogo.lab.gui
