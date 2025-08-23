#! /bin/sh

set -e

cd /root/

# 32-bit `jpackage` support
dpkg --add-architecture i386
apt update
apt install -y libc6:i386 libncurses5:i386 libstdc++6:i386 zlib1g-dev:i386

# Install general tools
apt install -y curl git wget

# Set up global SBT
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list
echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | tee /etc/apt/trusted.gpg.d/sbt.asc
apt update
apt install -y sbt

# Tooling for NL release
apt install -y aspell

# Add Python and numpy for LevelSpace extension
apt install -y python3 python3-pip
pip3 install numpy

# Install 'tzdata' without the interactive prompts about where you live (required by 'libglib' below)
DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends tzdata

# Required by Puppeteer/Chromium for generating NetLogo User Manual PDF
apt install -y libglib2.0-0 libnspr4 libnss3 libdbus-1-3 libatk1.0-0 libatk-bridge2.0-0 libcups2 libxkbcommon0 libxcomposite1 libxdamage1 libxfixes3 libxrandr2 libgbm1 libcairo2 libpango1.0-0 libasound2

# NPM installation for NL color picker
wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
. /root/.nvm/nvm.sh
nvm install 20.16

mkdir /root/jvm-store/

# Liberica 32-bit JDK for 32-bit release
wget https://download.bell-sw.com/java/17.0.15+10/bellsoft-jdk17.0.15+10-linux-i586.tar.gz
gunzip bellsoft*
tar -xf bellsoft*
rm bellsoft*
mv jdk-17.0.15 /root/jvm-store/liberica-jdk-17.0.15/

# OpenJDK 64-bit JDK for primary JDK and 64-bit release
wget https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz
gunzip openjdk*
tar -xf openjdk*
rm openjdk*
mv jdk-17.0.2 /root/jvm-store/openjdk-17.0.2/

# Configure Java for general NL build
export JAVA_HOME=/root/jvm-store/openjdk-17.0.2/

ln -s $JAVA_HOME/bin/java /usr/bin/java

# Initialize NetLogo repo
git clone https://github.com/NetLogo/NetLogo
cd NetLogo
git submodule update --init --recursive

# Set up Mathematica-Link
mv /root/JLink.jar /root/NetLogo/Mathematica-Link/

# Create JDK conf file for NL release
cat > /root/NetLogo/.jdks.yaml << EOF
- vendor:       "Liberica"
  version:      "17.0.15"
  architecture: "32"
  path:         "/root/jvm-store/liberica-jdk-17.0.15/"

- vendor:       "OpenJDK"
  version:      "17.0.2"
  architecture: "64"
  path:         "/root/jvm-store/openjdk-17.0.2/"
EOF

sbt dist/buildNetLogo

sbt "dist/packageLinuxAggregate OpenJDK_17.0.2_64"
mv /root/NetLogo/dist/target/downloadPages/*.tgz /root/NL-Linux-64.tgz

sbt "dist/packageLinuxAggregate Liberica_17.0.15_32"
mv /root/NetLogo/dist/target/downloadPages/*.tgz /root/NL-Linux-32.tgz
