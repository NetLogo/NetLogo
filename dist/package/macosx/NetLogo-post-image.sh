#!/bin/sh

cd ../images/`ls -1 ../images | head`
rm -rf NetLogo.app/Contents/Resources
mv NetLogo.app/Contents/Java/Resources NetLogo.app/Contents/
mv NetLogo.app/Contents/Java/PkgInfo NetLogo.app/Contents/
