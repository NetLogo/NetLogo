#!/bin/sh

cd ../images/`ls -1 ../images | head`
TARGET=`ls -1 | head`
rm -rf "$TARGET/Contents/Resources"
mv "$TARGET/Contents/Java/Resources" "$TARGET/Contents/"
mv "$TARGET/Contents/Java/PkgInfo"   "$TARGET/Contents/"
