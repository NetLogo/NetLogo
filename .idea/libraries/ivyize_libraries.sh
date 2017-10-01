#!/bin/bash

for f in `ls -1 | grep xml`; do
  URLS=$(cat $f | grep url | gsed 's;.*<root url="\(.*\)".*;\1;')
  for url in `echo $URLS`; do
    JAR_NAME=$(echo $url | gsed 's;jar://$PROJECT_DIR$/lib_idea/\(.*\)!\/;\1;')
    IVY_JAR=$(find $HOME/.ivy2 -name $JAR_NAME | head -1)
    RELATIVIZED_IVY_JAR_PATH=$(echo $IVY_JAR | gsed "s;${HOME}/.ivy2;;")
    IVY_JAR_URL="jar://\$IVY_HOME\$${RELATIVIZED_IVY_JAR_PATH}!/"
    gsed -i "s;$url;$IVY_JAR_URL;" $f
  done
done