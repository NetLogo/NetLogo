#!/bin/sh

# A script to fix permissions for lock files on Mac OS X
# Contributed by Dmitry Markman <dimitry.markman@verizon.net>
# Fri Aug 23 15:46:46 MDT 2002

curruser=`sudo id -p | grep 'login' | sed 's/login.//'`

if [ ! -d /var/spool/uucp ]
then
sudo mkdir /var/spool/uucp
fi

if [ ! -d /var/lock ]
then
sudo mkdir /var/lock
fi

sudo chgrp uucp /var/spool/uucp
sudo chmod 775 /var/spool/uucp
sudo chgrp uucp /var/lock
sudo chmod 775 /var/lock

if [ ! `sudo dscl -readpl / /groups/uucp users | grep $curruser > /dev/null` ]
then
  sudo dscl . append /groups/uucp GroupMembership $curruser
fi

