#!/bin/sh
cd "`dirname "$0"`"		# the copious quoting is for handling paths with spaces
# -server                       use server VM for highest performance
# -Xms16m -Xmx1024m             use up to 1GB RAM (edit to increase)
# -Dfile.encoding=UTF-8         ensure Unicode characters in model files are compatible cross-platform
# -classpath NetLogo.jar        specify main jar
# org.nlogo.headless.Main       specify we want headless, not GUI
# "$@"                          pass along any command line arguments
java -server -Xms16m -Xmx1024m -Dfile.encoding=UTF-8 -classpath NetLogo.jar org.nlogo.headless.Main "$@"
