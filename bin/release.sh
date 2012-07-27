#!/bin/bash -e

# -e makes the whole thing die with an error if any command does
# add -v if you want to see the commands as they happen

# all binaries we use
BUNZIP=bunzip2
CHMOD=chmod
CP=cp
CURL=curl
DU=du
FIND=find
GREP=grep
HDIUTIL=hdiutil
IJ=bin/install4jc
JAVA=java
LN=ln
LS=ls
MAKE=make
MKDIR=mkdir
MV=mv
OPEN=open
PACK200=pack200
PERL=perl
RM=rm
RSYNC=rsync
SED=sed
TAR=tar
XARGS=xargs

# other
SCALA_JAR=$HOME/.sbt/boot/scala-2.9.2/lib/scala-library.jar
IJVERSION=5.0.9
IJDIR="/Applications/install4j 5"
VM=windows-x86-1.6.0_33_server

# make sure we have proper versions of tools
# ("brew install htmldoc"; or if you don't want to involve homebrew,
# 1.8.27 is also available from htmldoc.org as a simple
# configure/make/make install)
if test `htmldoc --version` != 1.8.27 ;
then
  echo "htmldoc 1.8.27 not found"
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
  # make sure we have Install4J
  if [ ! -d "$IJDIR/jres" ]; then
    echo "$IJDIR/jres not found"
    echo "You'll need to install Install4j. Seth has the license key."
    exit 1
  fi
  # check install 4j version
  DESIRED_VERSION="install4j version 5.0.9 (build 5372), built on 2011-07-08"
  pushd "$IJDIR" > /dev/null
  FOUND_VERSION=`./$IJ --version`
  popd > /dev/null
  if test "$FOUND_VERSION" != "$DESIRED_VERSION" ;
  then
    echo "desired version: " $DESIRED_VERSION
    echo "found version: " $FOUND_VERSION
    exit 1
  fi
  # fetch VM pack if needed
  if [ ! -f "$IJDIR/jres/$VM.tar.gz" ]; then
    echo "fetching VM pack"
    pushd "$IJDIR/jres" > /dev/null
    $CURL -f -s -S -O "http://ccl.northwestern.edu/devel/"$VM.tar.gz
    popd > /dev/null
  fi
  # make sure VM pack is complete and not corrupt
  tar tzf "$IJDIR/jres/$VM.tar.gz" > /dev/null
fi

until [ -n "$REQUIRE_PREVIEWS" ]
do
  read -p "Require model preview images be present? " -n 1 ANSWER
  echo
  if [ "$ANSWER" == "y" ] || [ "$ANSWER" == "Y" ]; then
    REQUIRE_PREVIEWS=1
  fi
  if [ "$ANSWER" == "n" ] || [ "$ANSWER" == "N" ]; then
    REQUIRE_PREVIEWS=0
  fi
done

until [ -n "$DO_RSYNC" ]
do
  read -p "Rsync to CCL server when done? " -n 1 ANSWER
  echo
  if [ "$ANSWER" == "y" ] || [ "$ANSWER" == "Y" ]; then
    DO_RSYNC=1
  fi
  if [ "$ANSWER" == "n" ] || [ "$ANSWER" == "N" ]; then
    DO_RSYNC=0
  fi
done

# fail early if JLink.jar is missing
if [ ! -f Mathematica-Link/Makefile ]; then
  git submodule update --init Mathematica-Link
fi
if [ -f ~/nl.41/Mathematica\ Link/JLink.jar ]; then
  cp ~/nl.41/Mathematica\ Link/JLink.jar Mathematica-Link
fi
if [ ! -f Mathematica-Link/JLink.jar ]; then
  echo "Mathematica-Link/JLink.jar missing. copy it from a Mathematica installation (or the 4.1 branch, if you're a CCL'er)"
  echo "(it's needed to compile the link, but we don't have a license to distribute it)"
  exit 1
fi

# compile, build jars etc.
cd extensions
for FOO in *
do
  echo "cleaning extension" $FOO
  cd $FOO
  rm -f $FOO.jar $FOO.jar.pack.gz
  cd ..
done
cd ..
rm -f *.jar
./sbt clean all

# remember version number
export VERSION=`$JAVA -cp target/NetLogo.jar:$SCALA_JAR org.nlogo.headless.Main --version | $SED -e "s/NetLogo //"`
export DATE=`$JAVA -cp target/NetLogo.jar:$SCALA_JAR org.nlogo.headless.Main --builddate`
echo $VERSION":" $DATE
export COMPRESSEDVERSION=`$JAVA -cp target/NetLogo.jar:$SCALA_JAR org.nlogo.headless.Main --version | $SED -e "s/NetLogo //" | $SED -e "s/ //g"`

# make fresh staging area
$RM -rf tmp/netlogo-$COMPRESSEDVERSION
$MKDIR -p tmp/netlogo-$COMPRESSEDVERSION
cd tmp/netlogo-$COMPRESSEDVERSION

# put most of the files in
$CP -rp ../../docs .
$CP -p ../../dist/readme.txt .
$CP -p ../../dist/netlogo_logging.xml .
$CP -p ../../target/NetLogo.jar ../../target/HubNet.jar .
$CP ../../target/NetLogoLite.jar .
$PACK200 --modification-time=latest --effort=9 --strip-debug --no-keep-file-order --unknown-attribute=strip NetLogoLite.jar.pack.gz NetLogoLite.jar

# fill lib directory
$MKDIR lib
$CP -p \
  ../../lib_managed/jars/javax.media/jmf/jmf-2.1.1e.jar \
  ../../lib_managed/jars/asm/asm-all/asm-all-3.3.1.jar \
  ../../lib_managed/bundles/log4j/log4j/log4j-1.2.16.jar \
  ../../lib_managed/jars/org.picocontainer/picocontainer/picocontainer-2.13.6.jar \
  ../../lib_managed/jars/org.parboiled/parboiled-core/parboiled-core-1.0.2.jar \
  ../../lib_managed/jars/org.parboiled/parboiled-java/parboiled-java-1.0.2.jar \
  ../../lib_managed/jars/org.pegdown/pegdown/pegdown-1.1.0.jar \
  ../../lib_managed/jars/steveroy/mrjadapter/mrjadapter-1.2.jar \
  ../../lib_managed/jars/org.jhotdraw/jhotdraw/jhotdraw-6.0b1.jar \
  ../../lib_managed/jars/ch.randelshofer/quaqua/quaqua-7.3.4.jar \
  ../../lib_managed/jars/ch.randelshofer/swing-layout/swing-layout-7.3.4.jar \
  ../../lib_managed/jars/org.jogl/jogl/jogl-1.1.1.jar \
  ../../lib_managed/jars/org.gluegen-rt/gluegen-rt/gluegen-rt-1.1.1.jar \
  lib
$CP -p $SCALA_JAR lib/scala-library.jar

# Mathematica link stuff
$CP -rp ../../Mathematica-Link Mathematica\ Link
(cd Mathematica\ Link; NETLOGO=.. make) || exit 1
$RM Mathematica\ Link/JLink.jar

# stuff version number etc. into readme
$PERL -pi -e "s/\@\@\@VERSION\@\@\@/$VERSION/g" readme.txt
$PERL -pi -e "s/\@\@\@DATE\@\@\@/$DATE/g" readme.txt
$PERL -pi -e "s/\@\@\@UNIXNAME\@\@\@/netlogo-$COMPRESSEDVERSION/g" readme.txt

# include extensions
$MKDIR extensions
$CP -rp ../../extensions/[a-z]* extensions
$RM -rf extensions/sample extensions/sample-scala
$RM -rf extensions/*/{src,Makefile,manifest.txt,classes,tests.txt,README.md,build.xml,turtle.gif,.classpath,.project,.settings,project,target,build.sbt,*.zip,bin}
# Apple's license won't let us include this - ST 2/6/12
$RM -f extensions/qtj/QTJava.jar

# include models
$CP -rp ../../models .
$RM -rf models/README.md models/bin models/test

# blow away version control and Mac junk
$FIND models \( -name .DS_Store -or -name .gitignore -or -path \*/.git \) -print0 \
  | $XARGS -0 $RM -rf

# verify all VERSION sections are gone, as a guard against malformed
# sections missed by the previous step
$GREP -rw ^VERSION models && echo "no VERSION sections please; exiting" && exit 1
$GREP -rw \\\$Id models && echo "no \$Id please; exiting" && exit 1

# put copyright notices in code and/or info tabs
$LN -s ../../dist        # notarize script needs this
$LN -s ../../resources   # and this
$LN -s ../../scala       # and this
$LN -s ../../bin         # and this
../../models/bin/notarize.scala $REQUIRE_PREVIEWS || exit 1
$RM -f models/legal.txt
$RM dist resources scala bin

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
../../../bin/htmldoc.sh
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
( cd models/Sample\ Models     ; $CP -rp Biology/AIDS* Social\ Science ) || exit 1
( cd models/Sample\ Models     ; $CP -rp Networks/Team\ Assembly* Social\ Science ) || exit 1
( cd models/Sample\ Models     ; $CP -rp Biology/Evolution/Altruism* Social\ Science ) || exit 1
( cd models/Sample\ Models     ; $CP -rp Biology/Evolution/Cooperation* Social\ Science ) || exit 1
( cd models/Sample\ Models     ; $CP -rp Biology/Evolution/Unverified/Divide* Social\ Science/Unverified ) || exit 1
( cd models/Sample\ Models     ; $CP -rp Biology/Simple\ Birth* Social\ Science ) || exit 1
( cd models                    ; $MKDIR -p Curricular\ Models ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Chemistry\ \&\ Physics/GasLab Curricular\ Models ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Chemistry\ \&\ Physics/MaterialSim Curricular\ Models ) || exit 1
( cd models                    ; $CP -p Sample\ Models/Mathematics/Probability/ProbLab/*.{nlogo,png} Curricular\ Models/ProbLab ) || exit 1
( cd models                    ; $CP -p Sample\ Models/Mathematics/Probability/ProbLab/Unverified/* Curricular\ Models/ProbLab ) || exit 1
( cd models/Curricular\ Models ; $MKDIR -p EACH/Unverified ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Altruism* Curricular\ Models/EACH ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Cooperation* Curricular\ Models/EACH ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Unverified/Divide* Curricular\ Models/EACH/Unverified ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/System\ Dynamics/Unverified/Tabonuco* Sample\ Models/Biology/Unverified ) || exit 1

# BEAGLE curricular models
( cd models                    ; $CP -rp Sample\ Models/Biology/Wolf\ Sheep\ Predation* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Genetic\ Drift/GenDrift\ T\ interact* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Bug\ Hunt\ Speeds* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Bug\ Hunt\ Camouflage* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/*.jpg Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp HubNet\ Activities/Unverified/Guppy\ Spots* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp HubNet\ Activities/Unverified/aquarium.jpg Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp HubNet\ Activities/Bug\ Hunters\ Camouflage* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Daisyworld* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Mimicry* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Altruism* Curricular\ Models/BEAGLE\ Evolution ) || exit 1
( cd models                    ; $CP -rp Sample\ Models/Biology/Evolution/Cooperation* Curricular\ Models/BEAGLE\ Evolution ) || exit 1

# it'd be nice if there were an easier way to fool the model-index task
# into processing our directory where it is instead of having to bamboozle
# it like this with a temporary symbolic link - ST 6/18/12
( cd ../..
  mv models models.tmp
  ln -s tmp/netlogo-$COMPRESSEDVERSION/models
  ./sbt model-index
  rm models
  mv models.tmp models
) || exit 1

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
$FIND . \( -name .DS_Store -or -name .gitignore -or -path \*/.git \) -print0 \
  | $XARGS -0 $RM -rf
$FIND . -empty -print

# make sure no empty directories or files are
# lying around. do twice, once to print them all,
# the second time to halt if any are found
$FIND . -empty
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
$FIND . \( -name .DS_Store -or -name .gitignore -or -path \*/.git \) -print0 \
  | $XARGS -0 $RM -rf

# make the dmg
$CHMOD -R go+rX .
cd ..
$RM -rf dmg
$MKDIR dmg
$CP -rp netlogo-$COMPRESSEDVERSION dmg/NetLogo\ "$VERSION"
( cd dmg/NetLogo\ "$VERSION"; $LN -s docs/NetLogo\ User\ Manual.pdf ) || exit 1
$FIND dmg -name Windows     -print0 | $XARGS -0 $RM -rf
$FIND dmg -name Linux-amd64 -print0 | $XARGS -0 $RM -rf
$FIND dmg -name Linux-x86   -print0 | $XARGS -0 $RM -rf
$HDIUTIL create -quiet NetLogo\ "$VERSION".dmg -srcfolder dmg -volname NetLogo\ "$VERSION" -ov
$HDIUTIL internet-enable -quiet -yes NetLogo\ "$VERSION".dmg
$DU -h NetLogo\ "$VERSION".dmg
$RM -rf dmg
mv NetLogo\ "$VERSION".dmg $COMPRESSEDVERSION
cd netlogo-$COMPRESSEDVERSION

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
  "$IJDIR/$IJ" --quiet -r "$COMPRESSEDVERSION" -d "." NetLogo.install4j
  $CHMOD -R a+x *.exe
  $DU -h *.exe
  $MV *.exe ../$COMPRESSEDVERSION
fi

# make directory with web pages and so on
cd ..
$CP -p netlogo-$COMPRESSEDVERSION/{NetLogo,NetLogoLite}.jar netlogo-$COMPRESSEDVERSION/NetLogoLite.jar.pack.gz $COMPRESSEDVERSION
$CP -rp netlogo-$COMPRESSEDVERSION/docs $COMPRESSEDVERSION
$CP -rp netlogo-$COMPRESSEDVERSION/models $COMPRESSEDVERSION
if [ $WINDOWS -eq 1 ]
then
  $CP -p ../dist/windows.html $COMPRESSEDVERSION
fi
$CP -p ../dist/index.html $COMPRESSEDVERSION
$CP -p ../dist/title.jpg $COMPRESSEDVERSION
$CP -p ../dist/donate.png $COMPRESSEDVERSION
$CP -p ../dist/os-*.gif $COMPRESSEDVERSION
$CP -rp ../models/test/applet $COMPRESSEDVERSION
$CP $COMPRESSEDVERSION/NetLogoLite.jar $COMPRESSEDVERSION/NetLogoLite.jar.pack.gz $COMPRESSEDVERSION/applet
$CP ../target/HubNet.jar $COMPRESSEDVERSION/applet
$CP -rp netlogo-$COMPRESSEDVERSION/extensions/{sound,matrix,table,bitmap,gis} $COMPRESSEDVERSION/applet
$FIND $COMPRESSEDVERSION/applet \( -name .DS_Store -or -name .gitignore -or -path \*/.git \) -print0 \
  | $XARGS -0 $RM -rf
$RM -rf $COMPRESSEDVERSION/applet/*/classes
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

# blow away git stuff et al again
cd ../..
$FIND tmp/$COMPRESSEDVERSION \( -name .DS_Store -or -name .gitignore \) -print0 | $XARGS -0 $RM -rf

# done
if [ $DO_RSYNC -eq 1 ]; then
  $RSYNC -av --inplace --progress --delete tmp/$COMPRESSEDVERSION ccl.northwestern.edu:/usr/local/www/netlogo
else
  echo
  echo "to upload to CCL server, do:"
  echo "rsync -av --inplace --progress --delete tmp/$COMPRESSEDVERSION ccl.northwestern.edu:/usr/local/www/netlogo"
fi

echo
echo "to tag the release (changing 'master' if necessary):"
echo git tag -a -m $COMPRESSEDVERSION $COMPRESSEDVERSION master
echo git submodule foreach git tag -a -m $COMPRESSEDVERSION $COMPRESSEDVERSION master
echo
echo "and to push the tags:"
echo git push --tags
echo git submodule foreach git push --tags
echo
