#!/bin/bash

cd "`dirname "$0"`"

VERSION=`head -n1 version_number.txt`

#scp "behaviorsearch_$VERSION.tar.gz" "$SSCC:~/netlogo41/"
#ssh "$SSCC" "cd ~/netlogo41/ ; tar -xzf behaviorsearch_$VERSION.tar.gz"

#scp "behaviorsearch_$VERSION.tar.gz" "$CLUSTER:~/netlogo41/"
#ssh "$CLUSTER" "cd ~/netlogo41/ ; tar -xzf behaviorsearch_$VERSION.tar.gz"

scp "behaviorsearch_$VERSION.tar.gz" "$QUEST:~/netlogo41/"
ssh "$QUEST" "cd ~/netlogo41/ ; tar -xzf behaviorsearch_$VERSION.tar.gz"


