#! /bin/sh

set -e

cp $1 .

docker build -t nl-build .
docker run nl-build

docker cp -r nl-build:/root/NetLogo/dist/target/downloadPages out

docker rm nl-build
