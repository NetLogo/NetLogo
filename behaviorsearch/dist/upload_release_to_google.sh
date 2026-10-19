#!/bin/bash

VERSION=`head -n1 version_number.txt`

USER=`zenity --entry --text "username:" --entry-text "fsondahl"`
PW=`zenity --entry --text "password:"`


python googlecode_upload.py -s "BehaviorSearch ${VERSION} (for Windows)" -p behaviorsearch -u "$USER" -w "$PW" -l "Featured,Type-Installer,OpSys-Windows" "behaviorsearch_${VERSION}_installer.exe"

python googlecode_upload.py -s "BehaviorSearch ${VERSION} (for Mac)" -p behaviorsearch -u "$USER" -w "$PW" -l "Featured,Type-Archive,OpSys-OSX" "behaviorsearch_${VERSION}.zip"

python googlecode_upload.py -s "BehaviorSearch ${VERSION} (for Linux)" -p behaviorsearch -u "$USER" -w "$PW" -l "Featured,Type-Archive,OpSys-Linux" "behaviorsearch_${VERSION}.tar.gz"

