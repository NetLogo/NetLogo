#!/bin/bash

if [[ `uname -s` == *CYGWIN* ]] ; then
  CURR_DIR="$( cd "$( dirname "$0" )" && pwd )"
  export JAVA_HOME=`cygpath -up "\Java\jdk1.6.0_31"`
else
  CURR_DIR=`dirname $0`
  if [ `uname -s` = Linux ] ; then
    export JAVA_HOME=/usr/lib/jvm/java-6-sun
  else
    if [ `uname -s` = Darwin ] ; then
      export JAVA_HOME=`/usr/libexec/java_home -F -v1.6*`
    else
      export JAVA_HOME=/usr
    fi
  fi
fi

export PATH=$JAVA_HOME/bin:$PATH
JAVA=$JAVA_HOME/bin/java

# Most of these settings are fine for everyone
XSS=-Xss2m
XMX=-Xmx1536m
XX=-XX:MaxPermSize=512m
ENCODING=-Dfile.encoding=UTF-8
HEADLESS=-Djava.awt.headless=true
USE_QUARTZ=-Dapple.awt.graphics.UseQuartz=false
DISABLE_EXT_DIRS=-Djava.ext.dirs=
BOOT=xsbt.boot.Boot

# Windows/Cygwin users need these settings
if [[ `uname -s` == *CYGWIN* ]] ; then

  # While you might want the max heap size lower, you'll run out
  # of heap space from running the tests if you don't crank it up
  # (namely, from TestChecksums)
  XMX=-Xmx1350m
  SBT_LAUNCH=`cygpath -w $SBT_LAUNCH`

  # This gets SBT working properly in my heavily-modded version of Cygwin --JAB (2/7/2012)
  if [ "$TERM" = "xterm" ] ; then
    TERMINAL=-Djline.terminal=jline.UnixTerminal
  fi

fi

SBT_LAUNCH=$HOME/.sbt/sbt-launch-0.12.1.jar
URL='http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.12.1/sbt-launch.jar'

if [ ! -f $SBT_LAUNCH ] ; then
  echo "downloading" $URL
  mkdir -p $HOME/.sbt
  curl -s -S -f $URL -o $SBT_LAUNCH || exit
fi

# UseQuartz=false so that we get pixel for pixel identical drawings between OS's, so TestChecksums works - ST 6/9/10
"$JAVA" \
    $XSS $XMX $XX \
    $ENCODING \
    $JAVA_OPTS \
    $HEADLESS \
    $TERMINAL \
    $USE_QUARTZ \
    $DISABLE_EXT_DIRS \
    -classpath $SBT_LAUNCH \
    $BOOT "$@"
