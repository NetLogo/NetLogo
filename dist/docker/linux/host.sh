#! /bin/sh

set -e

if [ $(/usr/bin/id -u) -ne 0 ]; then
  echo "This script must be run as root (i.e.: sudo -E ./host.sh)"
  exit 1
fi

if [ -z "${NL_BUILD_VERSION}" ]; then
  echo "You must set the NL_BUILD_VERSION variable for naming the build artifacts (e.g. NL_BUILD_VERSION=7.0.0)"
  exit 1
fi

apt install -y docker.io
docker pull ubuntu:20.04

NL_DOCKER_ID=`sudo docker run -dit ubuntu:20.04 /bin/bash`
docker cp ./container.sh $NL_DOCKER_ID:/root/
docker exec $NL_DOCKER_ID bash -c "chmod +x /root/container.sh"
docker exec $NL_DOCKER_ID /root/container.sh

docker cp $NL_DOCKER_ID:/root/NL-Linux-64.tgz ./NetLogo-$NL_BUILD_VERSION-64.tgz
docker cp $NL_DOCKER_ID:/root/NL-Linux-32.tgz ./NetLogo-$NL_BUILD_VERSION-32.tgz

docker stop $NL_DOCKER_ID
docker rm $NL_DOCKER_ID

MOMMAS_BOY=`who mom likes | awk '{print $1}'`

chown $MOMMAS_BOY:$MOMMAS_BOY ./NetLogo-$NL_BUILD_VERSION-64.tgz
chown $MOMMAS_BOY:$MOMMAS_BOY ./NetLogo-$NL_BUILD_VERSION-32.tgz

su $MOMMAS_BOY -c "tar zxf ./NetLogo-$NL_BUILD_VERSION-64.tgz"
