#!/bin/sh -ev

# Normally "git submodule update --init", invoked by the Makefile, clones
# our submodules from read-only URLs.  But if you are a NetLogo committer,
# you want to clone from URLs that you have push access to.  Running this
# script after cloning the main repo will override the URLs in .git/config
# so you can push to all repos.

git config submodule.models.url git@git.assembla.com:models.git
git config submodule.Mathematica-Link.url git@github.com:NetLogo/Mathematica-Link.git
git config submodule.extensions/array.url git@github.com:/NetLogo/Array-Extension.git
git config submodule.extensions/bitmap.url git@github.com:/NetLogo/Bitmap-Extension.git
git config submodule.extensions/gis.url git@github.com:/NetLogo/GIS-Extension.git
git config submodule.extensions/gogo.url git@github.com:/NetLogo/GoGo-Extension.git
git config submodule.extensions/matrix.url git@github.com:/NetLogo/Matrix-Extension.git
git config submodule.extensions/network.url git@github.com:/NetLogo/Network-Extension.git
git config submodule.extensions/profiler.url git@github.com:/NetLogo/Profiler-Extension.git
git config submodule.extensions/qtj.url git@github.com:/NetLogo/QTJ-Extension.git
git config submodule.extensions/sample.url git@github.com:/NetLogo/Sample-Extension.git
git config submodule.extensions/sample-scala.url git@github.com:/NetLogo/Sample-Scala-Extension.git
git config submodule.extensions/sound.url git@github.com:/NetLogo/Sound-Extension.git
git config submodule.extensions/table.url git@github.com:/NetLogo/Table-Extension.git
