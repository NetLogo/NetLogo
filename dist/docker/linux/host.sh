#! /bin/sh

set -e

if [ $(/usr/bin/id -u) -ne 0 ]; then
  echo "This script must be run as root (i.e.: sudo -E ./host.sh)"
  exit 1
fi

if [ -z "${NL_BUILD_VERSION}" ]; then
  echo "You must set the NL_BUILD_VERSION variable for naming the build artifacts (e.g. NL_BUILD_VERSION=7.0.3)"
  exit 1
fi

if [ ! -x /usr/bin/docker ]; then

  echo "No Docker installation found.  Installing...."

  apt update
  apt install ca-certificates curl gnupg lsb-release
  mkdir -p /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

  apt update
  apt install -y docker-ce docker-ce-cli

fi

docker pull ubuntu:20.04
NL_DOCKER_ID=`sudo docker run -dit ubuntu:20.04 /bin/bash`
docker exec $NL_DOCKER_ID bash -c "mkdir /root/.ssh/"

docker cp ./container.sh $NL_DOCKER_ID:/root/
docker exec $NL_DOCKER_ID bash -c "chmod +x /root/container.sh"
docker exec -e NL_BUILD_VERSION=$NL_BUILD_VERSION $NL_DOCKER_ID /root/container.sh
docker cp $NL_DOCKER_ID:/root/NL-Linux-64.tgz ./NetLogo-$NL_BUILD_VERSION-64.tgz
docker cp $NL_DOCKER_ID:/root/NL-Linux-32.tgz ./NetLogo-$NL_BUILD_VERSION-32.tgz

docker stop $NL_DOCKER_ID
docker rm $NL_DOCKER_ID

MOMMAS_BOY=`who mom likes | awk '{print $1}'`

chown $MOMMAS_BOY:$MOMMAS_BOY ./NetLogo-$NL_BUILD_VERSION-64.tgz
chown $MOMMAS_BOY:$MOMMAS_BOY ./NetLogo-$NL_BUILD_VERSION-32.tgz

su $MOMMAS_BOY -c "tar zxf ./NetLogo-$NL_BUILD_VERSION-64.tgz"
