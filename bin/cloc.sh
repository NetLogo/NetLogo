#!/bin/sh

# count lines of code

if [ ! -f tmp/cloc.pl ] ; then
  mkdir -p tmp
  curl -sS 'http://ccl.northwestern.edu/devel/cloc-1.53.pl' -o tmp/cloc.pl
  chmod +x tmp/cloc.pl
fi

tmp/cloc.pl \
  --exclude-ext=m,xml,html,css,dtd \
  --exclude-dir=tmp,project/build/classycle,project/plugins/src_managed \
  --progress-rate=0 \
  .
