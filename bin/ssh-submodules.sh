#!/bin/sh -ev

# If you want to use SSH-based URLs for submodules instead of GitHub's
# normal HTTPS-based URLs, run this script.
#
# Note that at one time, SSH-based URLs were required for pushing, but that is no
# longer true.  For further details, see
# http://stackoverflow.com/questions/11041729/why-does-github-recommend-https-over-ssh

git config submodule.models.url git@github.com:/NetLogo/models.git

git config submodule.Mathematica-Link.url git@github.com:/NetLogo/Mathematica-Link.git

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

# The above seems not to be doing the trick unless you run it before cloning,
# which is hard to remember to do.  So we bring out the big hammer:

( cd models; git remote set-url origin git@github.com:/NetLogo/models.git )

( cd Mathematica-Link; git remote set-url origin git@github.com:/NetLogo/Mathematica-Link.git )

( cd extensions/array; git remote set-url origin git@github.com:/NetLogo/Array-Extension.git )
( cd extensions/bitmap; git remote set-url origin git@github.com:/NetLogo/Bitmap-Extension.git )
( cd extensions/gis; git remote set-url origin git@github.com:/NetLogo/GIS-Extension.git )
( cd extensions/gogo; git remote set-url origin git@github.com:/NetLogo/GoGo-Extension.git )
( cd extensions/matrix; git remote set-url origin git@github.com:/NetLogo/Matrix-Extension.git )
( cd extensions/network; git remote set-url origin git@github.com:/NetLogo/Network-Extension.git )
( cd extensions/profiler; git remote set-url origin git@github.com:/NetLogo/Profiler-Extension.git )
( cd extensions/qtj; git remote set-url origin git@github.com:/NetLogo/QTJ-Extension.git )
( cd extensions/sample; git remote set-url origin git@github.com:/NetLogo/Sample-Extension.git )
( cd extensions/sample-scala; git remote set-url origin git@github.com:/NetLogo/Sample-Scala-Extension.git )
( cd extensions/sound; git remote set-url origin git@github.com:/NetLogo/Sound-Extension.git )
( cd extensions/table; git remote set-url origin git@github.com:/NetLogo/Table-Extension.git )
