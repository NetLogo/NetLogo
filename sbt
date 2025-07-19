#!/bin/bash

# Cygwin is no longer supported for java 9 and beyond - AAB 11/09/2021

# Allow user to set these variables by exporting them in the environment  - AAB 11/09/2021
if [ -z "$JAVA_VERSION_NUMBER" ] ; then
 JAVA_VERSION_NUMBER=17
fi

if [ -z "$SBT_LAUNCH_VERSION" ] ; then
  SBT_LAUNCH_VERSION=1.10.11
fi


MSG_SET_JAVA_HOME="Please set JAVA_HOME to version ${JAVA_VERSION_NUMBER}"

if [[ `uname -s` = Linux && -z $JENKINS_URL ]] ; then
    HIGHEST_PRIORITY_JAVA=`update-alternatives --display javac | grep priority | grep -E "java${JAVA_VERSION_NUMBER}|java-${JAVA_VERSION_NUMBER}" | sort -g -k 4 | tail -1 | cut -d\  -f1`
  if [ -e "$HIGHEST_PRIORITY_JAVA" ] ; then
    export JAVA_HOME="${HIGHEST_PRIORITY_JAVA%/bin/javac}"
  # Because the version info looks something like 'openjdk version "11.0.11" 2021-04-20 LTS'
  # we need to be careful to grab the version number from the output
  elif ! $JAVA_HOME/bin/java -version 2>&1 | head -n 1 | grep " \"${JAVA_VERSION_NUMBER}\." >> /dev/null ; then
    echo "$MSG_SET_JAVA_HOME"
    exit
  fi
else
  if [ `uname -s` = Darwin ] ; then
    # if user hasn't already set JAVA_HOME, find it for them
    # otherwise use the previously set JAVA_HOME
    if [ -z "$JAVA_HOME" ] ; then
      export JAVA_HOME=`/usr/libexec/java_home -F -v${JAVA_VERSION_NUMBER}`
    fi

    # if it's still not set, then the correct version is not installed
    if [ -z "$JAVA_HOME" ] ; then
      echo "$MSG_SET_JAVA_HOME"
      exit
    fi
  fi
fi

export PATH=$JAVA_HOME/bin:$PATH
JAVA=$JAVA_HOME/bin/java

# Most of these settings are fine for everyone
XSS=-Xss10m
XMRP=-XX:MaxRAMPercentage=50
ENCODING=-Dfile.encoding=UTF-8
HEADLESS=-Djava.awt.headless=true
BOOT=xsbt.boot.Boot
GOGO_JAVA=-Dnetlogo.extensions.gogo.javaexecutable=$JAVA

SBT_LAUNCH_JAR=sbt-launch-${SBT_LAUNCH_VERSION}.jar
SBT_LAUNCH=$HOME/.sbt/$SBT_LAUNCH_JAR
URL="https://repo.maven.apache.org/maven2/org/scala-sbt/sbt-launch/$SBT_LAUNCH_VERSION/$SBT_LAUNCH_JAR"

if [ ! -f $BUILD_NUMBER ] ; then
  JAVA_OPTS="-Dsbt.log.noformat=true"
fi

if [ ! -f $SBT_LAUNCH ] ; then
  echo "downloading" $URL
  mkdir -p $HOME/.sbt
  curl -s -S -L -f $URL -o $SBT_LAUNCH || exit
fi


"$JAVA" \
    $XSS $XMRP $XX \
    $ENCODING \
    $JAVA_OPTS \
    $HEADLESS \
    $TERMINAL \
    -classpath $SBT_LAUNCH \
    $BOOT "$@"
