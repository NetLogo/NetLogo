#!/bin/sh

cd "$(dirname -- "$0")/.."
sbt "headless/runMain org.nlogo.headless.Shell"
