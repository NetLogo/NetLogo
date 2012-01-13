#!/bin/sh
cd "`dirname "$0"`"             # the copious quoting is for handling paths with spaces
# -Xmx1024m                     use up to 1GB RAM (edit to increase)
# -classpath NetLogo.jar        specify main jar (HubNet.jar is only for the client applet)
# -Dfile.encoding=UTF-8         ensure Unicode characters in model files are compatible cross-platform
# org.nlogo.hubnet.client.App   specify the client
# "$@"                          pass along any command line arguments
java -Xmx1024m -classpath NetLogo.jar -Dfile.encoding=UTF-8 org.nlogo.hubnet.client.App "$@"
