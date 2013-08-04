#!/bin/sh

rlwrap ./sbt \
  -Djline.terminal=jline.UnsupportedTerminal \
  'run-main org.nlogo.headless.Shell'
