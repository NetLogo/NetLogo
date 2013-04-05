#!/bin/sh

# "count lines of code"
#
# `brew install cloc` first if needed - ST 4/5/13

cloc \
  --exclude-ext=m,xml,html,css,dtd \
  --exclude-dir=tmp,project/build/classycle,project/plugins/src_managed \
  --progress-rate=0 \
  .
