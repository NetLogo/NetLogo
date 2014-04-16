#!/bin/sh

if [ -n "$1" ]; then
   ARG=" \"$1\""
fi

rlwrap ./sbt \
  -Djline.terminal=jline.UnsupportedTerminal \
  --warn \
  "run-main org.nlogo.headless.Shell $ARG"
