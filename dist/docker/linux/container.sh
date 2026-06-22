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

# By default `imagemagick` installs v6 (`convert`).  We ought to be using v7 (`magick`).
# But this is a passable solution, I suppose.
# Needed for generating the splash screen
apt install -y imagemagick
ln -s /usr/bin/convert /usr/local/bin/magick

# Install Yarn for Helio
curl -fsSL https://dl.yarnpkg.com/debian/pubkey.gpg | gpg --dearmor -o /usr/share/keyrings/yarn.gpg
echo "deb [signed-by=/usr/share/keyrings/yarn.gpg] https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
apt update
apt install -y yarn

# NPM installation for NL color picker
wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
. /root/.nvm/nvm.sh
nvm install 24.13

mkdir /root/jvm-store/

# Liberica 32-bit JDK for 32-bit release
wget https://download.bell-sw.com/java/21.0.11+11/bellsoft-jdk21.0.11+11-linux-i586.tar.gz
gunzip bellsoft*
tar -xf bellsoft*
rm bellsoft*
mv jdk-21.0.11 /root/jvm-store/liberica-jdk-21.0.11/

# OpenJDK 64-bit JDK for primary JDK and 64-bit release
wget https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_linux-x64_bin.tar.gz
gunzip openjdk*
tar -xf openjdk*
rm openjdk*
mv jdk-21.0.2 /root/jvm-store/openjdk-21.0.2/

# Configure Java for general NL build
export JAVA_HOME=/root/jvm-store/openjdk-21.0.2/

ln -s $JAVA_HOME/bin/java /usr/bin/java

# Initialize NetLogo repo
git clone https://github.com/NetLogo/NetLogo -b main-7.1 NetLogo
cd NetLogo
git submodule update --init --recursive

# Create JDK conf file for NL release
cat > /root/NetLogo/.jdks.yaml << EOF
- vendor:       "Liberica"
  version:      "21.0.11"
  architecture: "32"
  path:         "/root/jvm-store/liberica-jdk-21.0.11/"

- vendor:       "OpenJDK"
  version:      "21.0.2"
  architecture: "64"
  path:         "/root/jvm-store/openjdk-21.0.2/"
EOF

sbt netlogo/resources
sbt dist/buildNetLogo

sbt "dist/packageLinuxAggregate OpenJDK_21.0.2_64"
mv /root/NetLogo/dist/target/downloadPages/*.tgz /root/NL-Linux-64.tgz

sbt "dist/packageLinuxAggregate Liberica_21.0.11_32"
mv /root/NetLogo/dist/target/downloadPages/*.tgz /root/NL-Linux-32.tgz
