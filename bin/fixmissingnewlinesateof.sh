#!/bin/sh
find . -name .git -prune -o -path "*/target/*" -prune -o -name PkgInfo -prune -o -name .idea -prune -o -type f -print0 \
  | xargs -0 grep -ZIl . \
  | tr '\n' '\0' \
  | xargs -0 perl -ni -e 'chomp; print ($_,"\n")'
