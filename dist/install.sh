#!/bin/bash

cd "`dirname "$0"`"

VERSION=`head -n1 version_number.txt`

cp "behaviorsearch_$VERSION.tar.gz" /home/forrest/apps/netlogo/
cd /home/forrest/apps/netlogo/ 
tar -xzf "behaviorsearch_$VERSION.tar.gz"
rm "behaviorsearch_$VERSION.tar.gz"

