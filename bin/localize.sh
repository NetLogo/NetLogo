#!/bin/sh

# test to make sure the user provided the country code
if ! test $1
then
  echo "usage: ./localize.sh country-code"
  echo "example: ./localize.sh ja"
  exit 1
else
  export LANGUAGE_CODE=$1
fi

# test to make sure we know where NetLogo lives.
# if we don't know, try using /Applications/NetLogo 5.0beta1, and say so.
if ! (test "$NETLOGO_DIR")
then
  echo "NETLOGO_DIR variable not set. Trying /Applications/NetLogo 5.0.2"
  export NETLOGO_DIR=/Applications/NetLogo\ 5.0.2/
fi

# make sure the NetLogo directory that we are using exists.
if ! (test -d "$NETLOGO_DIR")
then
  echo "$NETLOGO_DIR not found...exiting."
  exit 1
fi

# test to find out if we know where the localization files are.
# if we don't know, we assume they are in this directory.
if ! test "$LOCALIZE_DIR"
then
  echo "LOCALIZE_DIR variable not set. Using the current working directory (`pwd`) instead"
  export LOCALIZE_DIR=`pwd`
fi

export GUI_FILE="$LOCALIZE_DIR/GUI_Strings_$LANGUAGE_CODE.txt"
export ERRORS_FILE="$LOCALIZE_DIR/Errors_$LANGUAGE_CODE.txt"

# make sure the GUI translations exist
if ! test -f "$GUI_FILE"
then
  echo "couldn't find GUI translations: $GUI_FILE...exiting"
  exit 1
fi

# make sure the errors translations exist
if ! test -f "$ERRORS_FILE"
then
  echo "couldn't find Errors translations: $ERRORS_FILE...exiting"
  exit 1
fi

# make a temp dir to do all the work in
rm -rf tmp
mkdir tmp
cd tmp

# copy the jar into the temp dir
cp "$NETLOGO_DIR/NetLogo.jar" .

# unpack the jar
echo "unpacking NetLogo.jar"
jar xf NetLogo.jar

# copy the new localized text files into the temp directory
cp "$LOCALIZE_DIR/GUI_Strings_$LANGUAGE_CODE.txt" .
cp "$LOCALIZE_DIR/Errors_$LANGUAGE_CODE.txt" .

# convert the text files to ascii
echo "converting translated files to ascii"
native2ascii -encoding UTF-8 "GUI_Strings_$LANGUAGE_CODE.txt" > "GUI_Strings_$LANGUAGE_CODE.properties"
native2ascii -encoding UTF-8 "Errors_$LANGUAGE_CODE.txt" > "Errors_$LANGUAGE_CODE.properties"

# remove the old jar (so that it doesn't end up inside the new jar)
rm NetLogo.jar

# repack (Make a new NetLogo.jar containing the new localization)
echo "repacking NetLogo.jar"
jar cmf META-INF/MANIFEST.MF NetLogo.jar .

# copy the new jar into the NetLogo directory, overwriting the original.
echo "Copying NetLogo.jar to $NETLOGO_DIR/NetLogo.jar"
cp NetLogo.jar "$NETLOGO_DIR"

# cleanup (leaving these commented so users can inspect the tmp dir to make sure everything went ok)
# cd ..
# rm -rf tmp

# done
echo "Success. OK to relaunch NetLogo."
