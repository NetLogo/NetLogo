#!/bin/bash -e

# -e makes the whole thing die with an error if any command does
# add -v if you want to see the commands as they happen

# all binaries we use
BUNZIP=bunzip2
CHMOD=chmod
CP=cp
DU=du
FIND=find
HDIUTIL=hdiutil
IJ=bin/install4jc
JAVA=java
LN=ln
LS=ls
MAKE=make
MKDIR=mkdir
MV=mv
OPEN=open
OSASCRIPT=osascript
PERL=perl
RM=rm
SED=sed
TAR=tar
TOUCH=touch
XARGS=xargs

# other
SCALA=2.8.1
IJVERSION=5.0.8
IJDIR=/Applications/install4j-$IJVERSION
VM=windows-x86-1.6.0_25_server

# make sure we have proper versions of tools
# (htmldoc 1.8.27 is available from htmldoc.org; it's a simple
# configure/make/make install)
if test `htmldoc --version` != 1.8.27 ;
then
  echo "htmldoc 1.8.27 not found"
  exit 1
fi

# maybe we should be using the "submodules" feature of git for this? - ST 5/7/11
if [ ! -d "Mathematica-Link" ]; then
  echo "do this first:"
  echo "git clone git@github.com:NetLogo/Mathematica-Link.git"
  exit 1
fi

if [ ! -f "Mathematica-Link/JLink.jar" ]; then
  echo "Mathematica-Link/JLink.jar missing. copy it from a Mathematica installation (or the 4.1 branch, if you're a CCL'er)"
  exit 1
fi

# ask user whether to build Windows installers
# (ordinarily you want to, but sometimes you want to 
# skip it, such as when testing changes to this script)
until [ -n "$WINDOWS" ]
do
  read -p "Do you want to build Windows installers? " -n 1 ANSWER
  echo
  if [ "$ANSWER" == "y" ] || [ "$ANSWER" == "Y" ]; then
    WINDOWS=1
  fi
  if [ "$ANSWER" == "n" ] || [ "$ANSWER" == "N" ]; then
    WINDOWS=0
  fi
done

if [ $WINDOWS -eq 1 ]; then
  echo "OK, building for all platforms (Windows, Mac, Linux/Unix)"
  # make sure we have proper VM pack
  if [ ! -f "$IJDIR/jres/$VM.tar.gz" ]; then
    echo "$IJDIR/jres/$VM.tar.gz not found"
    echo "You'll need to have Install4j installed. Seth has the license key."
    echo "as for the VM pack, you can grab it from http://ccl.northwestern.edu/devel/ using curl -O"
    echo "or if we don't have one for this Java version yet,"
    echo "you can make it from inside install4j, but only on a windows machine"
    echo "go to Project -> Create JRE Bundles"
    echo "for path e.g.: c:\\jdk1.6.0_25"
    echo "for java version e.g.: 1.6.0_25"
    echo "for custom id: server"
    exit 1
  fi
  if test "`$IJDIR/$IJ --version`" != "install4j version 5.0.8 (build 5311), built on 2011-04-13" ; 
  then
	  echo "install4j " $IJDIR/$IJ "not found"
	  exit 1
  fi
else
  echo "OK, no Windows, just Mac and Linux/Unix"
fi

# clean
$MAKE -s clean

# compile, build jars etc.
bin/sbt update
$MAKE -s
bin/sbt behaviorspace-sources
# zzz TODO $MAKE -s javadoc-public

# remember version number
export VERSION=`$JAVA -cp NetLogo.jar:tmp/scala-library-trimmed.jar org.nlogo.headless.Main --version | $SED -e "s/NetLogo //"`
export DATE=`$JAVA -cp NetLogo.jar:tmp/scala-library-trimmed.jar org.nlogo.headless.Main --builddate`
export COMPRESSEDVERSION=`$JAVA -cp NetLogo.jar:tmp/scala-library-trimmed.jar org.nlogo.headless.Main --version | $SED -e "s/NetLogo //" | $SED -e "s/ //g"`

# eject any leftover dmg's from last run
$OSASCRIPT -e "tell application \"Finder\"" -e "eject disk \"NetLogo\"" -e "end" > /dev/null 2>&1 || true
$OSASCRIPT -e "tell application \"Finder\"" -e "eject disk \"NetLogo "$COMPRESSEDVERSION"\"" -e "end" > /dev/null 2>&1 || true

# make fresh staging area
$RM -rf tmp/netlogo-$COMPRESSEDVERSION
$MKDIR -p tmp/netlogo-$COMPRESSEDVERSION
cd tmp/netlogo-$COMPRESSEDVERSION

# put most of the files in
$CP -rp ../../docs .

$CP -p ../../dist/readme.txt .
$CP -p ../../dist/netlogo_logging.xml .
$CP -p ../../NetLogo.jar ../../HubNet.jar .
$CP ../../NetLogoLite.jar .

$MKDIR lib
$CP -p ../../lib_managed/scala_$SCALA/compile/jmf-2.1.1e.jar ../../lib_managed/scala_$SCALA/compile/asm-all-3.3.1.jar ../../lib_managed/scala_$SCALA/compile/log4j-1.2.16.jar ../../lib_managed/scala_$SCALA/compile/picocontainer-2.11.1.jar ../../lib_managed/scala_$SCALA/compile/parboiled-core-0.11.0.jar ../../lib_managed/scala_$SCALA/compile/parboiled-java-0.11.0.jar ../../lib_managed/scala_$SCALA/compile/pegdown-0.9.1.jar ../../lib_managed/scala_$SCALA/compile/mrjadapter-1.2.jar ../../lib_managed/scala_$SCALA/compile/jhotdraw-6.0b1.jar ../../lib_managed/scala_$SCALA/compile/quaqua-7.3.4.jar ../../lib_managed/scala_$SCALA/compile/swing-layout-7.3.4.jar ../../lib_managed/scala_$SCALA/compile/jogl-1.1.1.jar ../../lib_managed/scala_$SCALA/compile/gluegen-rt-1.1.1.jar lib
$CP -p ../../BehaviorSpace.jar ../../BehaviorSpace-src.zip lib
$CP -p ../../tmp/scala-library-trimmed.jar lib/scala-library.jar

# NLink stuff
(cd ../../Mathematica-Link; NETLOGO=.. SCALA_JAR=../tmp/scala-library-trimmed.jar make)
$MKDIR Mathematica\ Link
$CP -rp ../../Mathematica-Link/* Mathematica\ Link
$RM Mathematica\ Link/JLink.jar

# stuff version number etc. into readme
$PERL -pi -e "s/\@\@\@VERSION\@\@\@/$VERSION/g" readme.txt
$PERL -pi -e "s/\@\@\@DATE\@\@\@/$DATE/g" readme.txt
$PERL -pi -e "s/\@\@\@UNIXNAME\@\@\@/netlogo-$COMPRESSEDVERSION/g" readme.txt

# include extensions
$MKDIR extensions
$CP -rp ../../extensions/[a-z]* extensions
$RM -rf extensions/*/classes

# include models
$CP -rp ../../models .

# blow away version control and Mac junk
$FIND models \( -path \*/.svn -or -name .DS_Store -or -path \*/.git \) -print0 \
  | $XARGS -0 $RM -rf

# verify all VERSION sections are gone, as a guard against malformed
# sections missed by the previous step
grep -rw ^VERSION models && echo "no VERSION sections please; exiting" && exit 1
grep -rw \\\$Id models && echo "no \$Id please; exiting" && exit 1

# put copyright notices in procedures and/or info tabs
ln -s ../../dist        # notarize script needs this
ln -s ../../resources   # and this
ln -s ../../scala       # and this
ln -s ../../bin         # and this
../../bin/notarize.scala || exit 1
rm dist resources scala bin

# build the PDF with the proper version numbers inserted everywhere
$PERL -p -i -e "s/\@\@\@VERSION\@\@\@/$VERSION/g" docs/*.html
$PERL -p -i -e "s/\@\@\@DATE\@\@\@/$DATE/g" docs/headings.html
$PERL -p -i -e 's/NetLogo User Manual:/NetLogo $ENV{"VERSION"} User Manual:/' docs/*.html
$PERL -0 -p -i -e 's|<title>.+?NetLogo User Manual.+?</title>|<title>NetLogo $ENV{"VERSION"} User Manual</title>|gs' docs/*.html
$PERL -p -i -e 's/\<blockquote\>/\<p align="center"\>\<table width="90%" border="1" cellpadding="15" cellspacing="0" bordercolor="black"\>\<tr\>\<td bgcolor="#FAFADC"\>/' docs/*.html
$PERL -p -i -e 's/\<\/blockquote\>/\<\/td\>\<\/tr\>\<\/table\>/' docs/*.html
$PERL -0 -p -i -e 's|<div class="version">.+?</div>||gs;' docs/*.html
$PERL -p -i -e "s/\<h3\>/\<p\>\<hr\>\<h3\>/" docs/dictionary.html

cd docs
echo "-- we ignore htmldoc errors because the docs contain a bunch"
echo "-- of links to the javadocs, but the javadocs aren't included"
echo "-- in the PDF so htmldoc flags those as broken links."
echo "-- please be on the lookout for other kinds of errors."
../../../bin/htmldoc.sh || echo "htmldoc errors ignored"
cd ..

# blow away the docs directory messed up by the PDF generation process
# and make a new one
$RM -rf docs
$CP -rp ../../docs .
$MV NetLogo\ User\ Manual.pdf docs/
$PERL -p -i -e "s/\@\@\@VERSION\@\@\@/$VERSION/g" docs/*.html
$PERL -p -i -e "s/\@\@\@VERSION\@\@\@/$VERSION/g" docs/dict/*.html
$PERL -p -i -e "s/\@\@\@DATE\@\@\@/$DATE/g" docs/headings.html
$PERL -p -i -e 's/NetLogo User Manual:/NetLogo $ENV{"VERSION"} User Manual:/' docs/*.html
$PERL -p -i -e 's/NetLogo User Manual&nbsp;/NetLogo $ENV{"VERSION"} User Manual&nbsp;/' docs/*.html
# we use -0 so that the regexes can extend across line breaks
$PERL -0 -p -i -e 's|<title>.+?NetLogo User Manual.+?</title>|<title>NetLogo $ENV{"VERSION"} User Manual</title>|gs' docs/*.html

# put models in multiple categories
( cd models/Sample\ Models     ; $CP -rp Biology/AIDS* Social\ Science )
( cd models/Sample\ Models     ; $CP -rp Networks/Team\ Assembly* Social\ Science )
( cd models/Sample\ Models     ; $CP -rp Biology/Evolution/Altruism* Social\ Science )
( cd models/Sample\ Models     ; $CP -rp Biology/Evolution/Cooperation* Social\ Science )
( cd models/Sample\ Models     ; $CP -rp Biology/Evolution/Unverified/Divide* Social\ Science/Unverified )
( cd models/Sample\ Models     ; $CP -rp Biology/Simple\ Birth* Social\ Science )
( cd models                    ; $MKDIR -p Curricular\ Models )
( cd models                    ; $CP -rp Sample\ Models/Chemistry\ \&\ Physics/GasLab Curricular\ Models )
( cd models                    ; $CP -rp Sample\ Models/Chemistry\ \&\ Physics/MaterialSim Curricular\ Models )
echo "a warning here about Unverified not being copied is OK"
( cd models                    ; $CP -p Sample\ Models/Mathematics/Probability/ProbLab/* Curricular\ Models/ProbLab )
( cd models                    ; $CP -p Sample\ Models/Mathematics/Probability/ProbLab/Unverified/* Curricular\ Models/ProbLab )
( cd models/Curricular\ Models ; $MKDIR -p EACH/Unverified )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Altruism* Curricular\ Models/EACH )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Cooperation* Curricular\ Models/EACH )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Unverified/Divide* Curricular\ Models/EACH/Unverified )
( cd models                    ; $CP -rp Sample\ Models/System\ Dynamics/Unverified/Tabonuco* Sample\ Models/Biology/Unverified )

# BEAGLE curricular models
( cd models                    ; $CP -rp Sample\ Models/Biology/Wolf\ Sheep\ Predation* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Genetic\ Drift/GenDrift\ T\ interact* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Bug\ Hunt\ Speeds* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Bug\ Hunt\ Camouflage* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/*.jpg Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp HubNet\ Activities/Unverified/Guppy\ Spots* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp HubNet\ Activities/Unverified/aquarium.jpg Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp HubNet\ Activities/Bug\ Hunters\ Camouflage* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp Sample\ Models/Biology/Daisyworld* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Mimicry* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Altruism* Curricular\ Models/BEAGLE\ Evolution )
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Cooperation* Curricular\ Models/BEAGLE\ Evolution )

( cd ../.. ; bin/sbt "model-index tmp/netlogo-$COMPRESSEDVERSION/models/" )

# add JOGL native library for Linux
$CP -r ../../lib/Linux-amd64 lib/Linux-amd64
$CP -r ../../lib/Linux-x86 lib/Linux-x86

# move linux-only stuff into the package
$CP -p ../../dist/netlogo.sh .
$CP -p ../../dist/netlogo-headless.sh .
$CP -p ../../dist/netlogo-3D.sh .
$CP -p ../../dist/hubnet.sh .
$CP -p ../../dist/icon.ico .

# blow away version control and Mac junk
$FIND . \( -path \*/.svn -or -name .DS_Store -or -path \*/.git \) -print0 \
  | $XARGS -0 $RM -rf
$FIND . -path \*/.svn -prune -o -empty -print

# make sure no empty directories or files are
# lying around. do twice, once to print them all,
# the second time to halt if any are found
find . -empty
if [ "`find . -empty`" != "" ]
then
  echo "empty files/directories found; exiting"
  exit 1
fi

# make directory for web pages & packages
cd ..
$RM -rf $COMPRESSEDVERSION
mkdir $COMPRESSEDVERSION

# make tarball
$CHMOD -R go+rX netlogo-$COMPRESSEDVERSION
$RM -rf $COMPRESSEDVERSION/netlogo-$COMPRESSEDVERSION.tar.gz
# the ._ thing here is to avoid including Mac metadata/resource fork junk - ST 10/6/05
# the qtj extension doesn't work on linux
$TAR czf $COMPRESSEDVERSION/netlogo-$COMPRESSEDVERSION.tar.gz --exclude ._\* --exclude qtj --exclude Mac\ OS\ X --exclude Windows netlogo-$COMPRESSEDVERSION
$DU -h $COMPRESSEDVERSION/netlogo-$COMPRESSEDVERSION.tar.gz 
cd netlogo-$COMPRESSEDVERSION

# done with Unix release; now do Mac release

# remove Unix-only stuff
$RM -rf lib/Linux*
$RM icon.ico
$RM netlogo.sh
$RM netlogo-headless.sh
$RM netlogo-3D.sh
$RM hubnet.sh

# add in Mac-only stuff
$CP -r ../../lib/Mac\ OS\ X/ lib/Mac\ OS\ X/
$CP -rp ../../dist/NetLogo.app .
$CP -rp ../../dist/HubNet\ Client.app .
$PERL -pi -e "s/org.nlogo.NetLogoDEVEL/org.nlogo.NetLogo/" NetLogo.app/Contents/Info.plist
$PERL -pi -e "s@<key>CFBundleVersion</key> <string>1.0</string>@<key>CFBundleVersion</key> <string>$VERSION</string>@" NetLogo.app/Contents/Info.plist
$CP -rp ./NetLogo.app ./NetLogo\ 3D.app
$PERL -pi -e "s/org.nlogo.NetLogo/org.nlogo.NetLogo3D/" NetLogo\ 3D.app/Contents/Info.plist
$PERL -pi -e "s@<key>CFBundleTypeExtensions</key> <array> <string>nlogo</string> </array>@<key>CFBundleTypeExtensions</key> <array> <string>nlogo3d</string> </array>@" NetLogo\ 3D.app/Contents/Info.plist
$PERL -pi -e "s/-Dapple.awt.graphics.UseQuartz=true/-Dapple.awt.graphics.UseQuartz=true -Dorg.nlogo.is3d=true/" NetLogo\ 3D.app/Contents/Info.plist
$PERL -pi -e "s/org.nlogo.HubNetClientDEVEL/org.nlogo.HubNetClient/" HubNet\ Client.app/Contents/Info.plist
$PERL -pi -e "s@<key>CFBundleVersion</key> <string>1.0</string>@<key>CFBundleVersion</key> <string>$VERSION</string>@" HubNet\ Client.app/Contents/Info.plist
$CP -rp NetLogo.app NetLogo\ Logging.app
$PERL -pi -e  "s@<string>org.nlogo.app.App</string>@<string>org.nlogo.app.App</string> <key>Arguments</key> <string>--logging netlogo_logging.xml</string>@" NetLogo\ Logging.app/Contents/Info.plist
$MV NetLogo.app NetLogo\ "$VERSION".app
$MV NetLogo\ Logging.app NetLogo\ Logging\ "$VERSION".app
$MV HubNet\ Client.app HubNet\ Client\ "$VERSION".app
$MV NetLogo\ 3D.app NetLogo\ 3D\ "$VERSION"\.app

# blow away version control and Mac junk again
$FIND . \( -path \*/.svn -or -name .DS_Store -or -path \*/.git \) -print0 \
  | $XARGS -0 $RM -rf

# make the dmg
$CHMOD -R go+rX .
cd ../$COMPRESSEDVERSION
$RM -rf NetLogo\ "$VERSION".dmg
$CP ../../dist/NetLogo.sparseimage.bz2 .
$BUNZIP NetLogo.sparseimage.bz2
$HDIUTIL attach -quiet NetLogo.sparseimage
$CP -rp ../netlogo-$COMPRESSEDVERSION/* /Volumes/NetLogo/NetLogo
$FIND /Volumes/NetLogo/NetLogo -name Windows -print0 | $XARGS -0 $RM -rf
$FIND /Volumes/NetLogo/NetLogo -name Linux-amd64 -print0 | $XARGS -0 $RM -rf
$FIND /Volumes/NetLogo/NetLogo -name Linux-x86 -print0 | $XARGS -0 $RM -rf
sync ; sleep 5 ; sync    # I get zeroed-out files sometimes resulting in 300K dmg... this is blind stab to try to avoid - ST 1/27/05; since adding this problem has not reoccurred - ST 8/2/05
$OSASCRIPT -e "tell application \"Finder\"" -e "make new alias file at folder \"NetLogo\" of disk \"NetLogo\" to file \"NetLogo User Manual.pdf\" of folder \"docs\" of folder \"NetLogo\" of disk \"NetLogo\"" -e "end"
$OSASCRIPT -e "tell application \"Finder\"" -e "set name of folder \"NetLogo\" of disk \"NetLogo\" to \"NetLogo ""$VERSION""\"" -e "end"
$OSASCRIPT -e "tell application \"Finder\"" -e "set name of disk \"NetLogo\" to \"NetLogo ""$VERSION""\"" -e "end"
$FIND /Volumes/NetLogo\ "$VERSION" -name .Trashes -prune -o -print0 | $XARGS -0 $CHMOD go=rX
sync ; sleep 10 ; sync    # I got an image without the version number in the volume name once... another blind stab - ST 1/27/05; since adding this problem has not reoccurred - ST 8/2/05
$OSASCRIPT -e "tell application \"Finder\"" -e "eject disk \"NetLogo ""$VERSION""\"" -e "end"
sync ; sleep 10 ; sync    # same as above
$HDIUTIL convert -quiet NetLogo.sparseimage -format UDZO -o NetLogo\ "$VERSION".dmg
$RM NetLogo.sparseimage
$HDIUTIL internet-enable -quiet -yes NetLogo\ "$VERSION".dmg
$DU -h NetLogo\ "$VERSION".dmg
cd ../netlogo-$COMPRESSEDVERSION


# remove Mac-only stuff
$RM -r *.app
$RM -rf lib/Mac\ OS\ X/

# Mac done. Windows time!

# convert readme to Windows line endings
$PERL -p -i -e "s/\n/\r\n/g" readme.txt

# add Windows-only stuff, remove others
$CP -r ../../lib/Windows/ lib/Windows
$FIND . -name Linux-amd64 -print0 | $XARGS -0 $RM -rf
$FIND . -name Linux-x86 -print0 | $XARGS -0 $RM -rf
$FIND . -name Mac\ OS\ X -print0 | $XARGS -0 $RM -rf

# add stuff for install4j
$CP -rp ../../dist/NetLogo.install4j .
$MKDIR dist
$CP -rp ../../dist/hnicon32.gif dist
$CP -rp ../../dist/icon32.gif dist
$CP -rp ../../dist/index.html dist
$CP -rp ../../dist/donate.png dist
$CP -rp ../../dist/model.ico dist
$CP -rp ../../dist/os-mac.gif dist
$CP -rp ../../dist/os-other.gif dist
$CP -rp ../../dist/os-win.gif dist
$CP -rp ../../dist/title.jpg dist
$CP -rp ../../dist/os-mac.gif dist
$CP -rp ../../dist/windows.html dist
$CP -rp ../../dist/icon.ico dist

$CHMOD -R go+rX .

if [ $WINDOWS -eq 1 ]
then
  $PERL -pi -e "s/\@\@\@VM\@\@\@/$VM/g" NetLogo.install4j
  $IJDIR/$IJ -r "$COMPRESSEDVERSION" -d "." NetLogo.install4j
  $CHMOD -R a+x *.exe
  $MV *.exe ../$COMPRESSEDVERSION
fi

# make directory with web pages and so on
cd ..
$CP -p netlogo-$COMPRESSEDVERSION/{NetLogo,NetLogoLite}.jar $COMPRESSEDVERSION
$CP -rp netlogo-$COMPRESSEDVERSION/docs $COMPRESSEDVERSION
$CP -rp netlogo-$COMPRESSEDVERSION/models $COMPRESSEDVERSION
if [ $WINDOWS -eq 1 ]
then
  $CP -p ../dist/windows.html $COMPRESSEDVERSION
fi
$CP -p ../dist/index.html $COMPRESSEDVERSION
$CP -p ../dist/title.jpg $COMPRESSEDVERSION
$CP -p ../dist/os-*.gif $COMPRESSEDVERSION
$CP -rp ../test/applet $COMPRESSEDVERSION
$CP ../NetLogoLite.jar $COMPRESSEDVERSION/applet
$CP ../HubNet.jar $COMPRESSEDVERSION/applet
$CP -rp ../extensions/{sound,matrix,table,bitmap,gis} $COMPRESSEDVERSION/applet
$RM -r $COMPRESSEDVERSION/applet/*/classes
$CP -rp ../models/Code\ Examples/GIS/data $COMPRESSEDVERSION/applet
$CP -p ../Mathematica-Link/NetLogo-Mathematica\ Tutorial.pdf $COMPRESSEDVERSION/docs

# stuff version number and date into web page
cd $COMPRESSEDVERSION
$PERL -pi -e "s/\@\@\@VERSION\@\@\@/$VERSION/g" *.html
$PERL -pi -e "s/\@\@\@COMPRESSEDVERSION\@\@\@/$COMPRESSEDVERSION/g" *.html
$PERL -pi -e "s/\@\@\@DATE\@\@\@/$DATE/g" *.html

# stuff download sizes into web page
if [ $WINDOWS -eq 1 ]; then
  SIZE=`du -h *Installer.exe | cut -f 1 | sed -e "s/^ *//"`
  $PERL -pi -e "s/\@\@\@SIZE1\@\@\@/$SIZE/" *.html
  SIZE=`du -h *_NoVM.exe | cut -f 1 | sed -e "s/^ *//"`
  $PERL -pi -e "s/\@\@\@SIZE2\@\@\@/$SIZE/" *.html
fi
SIZE=`du -h *.dmg | cut -f 1 | sed -e "s/^ *//"`
$PERL -pi -e "s/\@\@\@SIZE3\@\@\@/$SIZE/" *.html
SIZE=`du -h *.tar.gz | cut -f 1 | sed -e "s/^ *//"`
$PERL -pi -e "s/\@\@\@SIZE4\@\@\@/$SIZE/" *.html

# make world-readable
$CHMOD -R go+rX .

# blow away svn et al stuff again
cd ../..
$FIND tmp/$COMPRESSEDVERSION \( -path \*/.svn -or -name .DS_Store \) -print0 | $XARGS -0 $RM -rf

# done
echo "now upload to ccl, like this:"
echo "rsync -av --progress --delete tmp/$COMPRESSEDVERSION ccl.northwestern.edu:/usr/local/www/netlogo"
