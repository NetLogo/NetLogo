#!/bin/bash

VERSION=`head -n1 version_number.txt`
cd "`dirname "$0"`"

if [[ `pwd` != *dist ]]
then
  echo "Script must be run from the 'dist' folder";
  exit 1
fi

ant -f makejar.xml

rm -rf behaviorsearch
mkdir behaviorsearch
cp ../behaviorsearch*.jar behaviorsearch/

cp ../LICENSE.TXT behaviorsearch/
cp ../README.TXT behaviorsearch/
cp ../CREDITS.TXT behaviorsearch/

rsync -a --exclude=.svn --exclude="*~" ./dist_include/ behaviorsearch/

#lib folder
rsync -a --exclude=.svn ../lib/*.jar behaviorsearch/lib/
rsync -a --exclude=.svn ../lib/*.txt behaviorsearch/lib/

#resources
rsync -a --exclude=.svn ../resources behaviorsearch/

#source code
rsync -a --exclude=.svn --exclude="*~" ../src behaviorsearch/

#examples
rsync -a --exclude=.svn --exclude="*~" ../examples behaviorsearch/

#documentation
rsync -a --exclude=.svn --exclude="*~" ../documentation behaviorsearch/

#Also, copy documentation to web site folder (but remember to upload!)
echo "Copying documentation files to /home/forrest/web_localcopy/behaviorsearch/"
rsync -a --exclude=.svn --exclude="*~" ../documentation /home/forrest/web_localcopy/behaviorsearch/


#Make ".command" files for the Mac
cp behaviorsearch/behaviorsearch_gui.sh behaviorsearch/behaviorsearch_gui.command
cp behaviorsearch/behaviorsearch-headless.sh behaviorsearch/behaviorsearch-headless.command

echo "Creating behaviorsearch_${VERSION}.tar.gz"
rm "behaviorsearch_${VERSION}.tar.gz"
tar -czf "behaviorsearch_${VERSION}.tar.gz" behaviorsearch --exclude "*.bat" --exclude "*.exe"

echo "Creating behaviorsearch_${VERSION}.zip"
rm "behaviorsearch_${VERSION}.zip"
zip -rq "behaviorsearch_${VERSION}.zip" behaviorsearch -x "*.bat" -x "*.exe"

#replace the first line of our NSIS installer file with the proper version number!
sed -i -e "1c \!define MUI_VERSION \"${VERSION}\"" win_installer.nsi

echo "Creating behaviorsearch_${VERSION}_installer.exe"
# use "makensis" to create windows executable.
makensis -V2 win_installer.nsi

##No longer copying files to web downloads... upload them to Google Code instead, using another script.

#echo "Copying setup files to /home/forrest/web_localcopy/behaviorsearch/"
cp "behaviorsearch_${VERSION}.tar.gz" /home/forrest/web_localcopy/behaviorsearch/downloads/
cp "behaviorsearch_${VERSION}.zip" /home/forrest/web_localcopy/behaviorsearch/downloads/
cp "behaviorsearch_${VERSION}_installer.exe" /home/forrest/web_localcopy/behaviorsearch/downloads/

#and copy LICENSE.TXT & RELEASE_NOTES.TXT files to website though...
cp ../LICENSE.TXT /home/forrest/web_localcopy/behaviorsearch/
cp ../RELEASE_NOTES.TXT /home/forrest/web_localcopy/behaviorsearch/
