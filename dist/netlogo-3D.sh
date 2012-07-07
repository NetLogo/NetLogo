#!/bin/sh
cd "`dirname "$0"`"             # the copious quoting is for handling paths with spaces
# -Djava.library.path=./lib     ensure JOGL can find native libraries
# -Djava.ext.dir=               ignore any existing JOGL installation
# -XX:MaxPermSize=128m          avoid OutOfMemory errors for large models
# -Xmx1024m                     use up to 1GB RAM (edit to increase)
# -Dfile.encoding=UTF-8         ensure Unicode characters in model files are compatible cross-platform
# -Dorg.nlogo.is3d=true         run 3D NetLogo
# -jar NetLogo.jar              specify main jar
# "$@"                          pass along any command line arguments
java -Djava.library.path=./lib -Djava.ext.dir= -XX:MaxPermSize=128m -Xmx1024m -Dfile.encoding=UTF-8 -Dorg.nlogo.is3d=true -jar NetLogo.jar "$@"
