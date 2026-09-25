#! /bin/bash

set -e

pushd `pwd` > /dev/null

DIR="$(cd "$(dirname "$0")" && pwd)"
cd $DIR

OUTPUT_DIR=./out/
BUILD_INFO=$OUTPUT_DIR/.last-build

CURR_HASH=`find ./html/ ./stylesheets/ ./typescripts/ ./package.json ./stage.sh ./tsconfig.json ./webpack.config.mjs ./parser/netlogo.grammar -type f -print0 | sort -z | xargs -0 sha1sum | sha1sum | cut -d ' ' -f1`
LAST_HASH=notfound

if [ -f $BUILD_INFO ]; then
  LAST_HASH=$(<$BUILD_INFO)
fi

if ! command -v npm 2>&1 >/dev/null
then
  echo "Required command 'npm' not found.  It is recommended to install NPM through NPX, which can be found here: https://github.com/nvm-sh/nvm .  Check the 'engines' section of this repository's './package.json' file to ensure that you install the correct version of Node/NPM."
  exit 1
fi

if [ "$CURR_HASH" != "$LAST_HASH" ]; then
  rm -rf ./dist/

  npm ci
  npm run build

  rm -rf $OUTPUT_DIR
  mkdir $OUTPUT_DIR

  cp ./html/index.html $OUTPUT_DIR

  mkdir $OUTPUT_DIR/stylesheets/
  cp ./stylesheets/*.css $OUTPUT_DIR/stylesheets/

  mkdir $OUTPUT_DIR/javascripts/
  cp ./dist/typescripts/*.js $OUTPUT_DIR/javascripts/

  npx webpack-cli

  echo "Code Tab built successfully!"
fi

mkdir -p $OUTPUT_DIR
echo $CURR_HASH > $BUILD_INFO

popd > /dev/null
