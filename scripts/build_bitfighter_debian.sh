#!/bin/bash
#
# Script to get set-up, build, and run the development version of bitfighter
# on debian-based systems
#
# To use, run it like so:
#
# wget --no-check-certificate -O- https://tools.bitfighter.googlecode.com/hg/scripts/build_bitfighter_debian.sh | sh
#
# Or, if you want to run against a server-side clone:
#
# wget --no-check-certificate -O- https://tools.bitfighter.googlecode.com/hg/scripts/build_bitfighter_debian.sh | sh -s URL_TO_CLONE
#

url=$1

# If no clone URL provided, then use bitfighter main URL
if [ -z $url ]; then
    url="https://code.google.com/p/bitfighter"
fi

echo $url

# Refresh update catalog
echo "=> Refreshing package catalog"
sudo apt-get update

# install deps
echo "=> Installing dependencies"
sudo apt-get --yes install libsdl1.2-dev zlib1g-dev libpng12-dev libopenal-dev libvorbis-dev libspeex-dev libmodplug-dev cmake build-essential mercurial

# checkout latest source code
echo "=> Cloning the repository from $url"
hg clone $url bitfighter-hg

# begin building. time to make a sandwhich.
echo "=> Running CMake"
cd bitfighter-hg/build
cmake .. -DCMAKE_BUILD_TYPE=Debug

echo "=> Building the source code"
make -j2

# finally, make some symbolic links so we can launch bitfighter from the exe directory
cd ../exe/
echo "=> Linking resources"
for file in `ls -1 ../resource` ; do ln -s ../resource/$file ; done

echo "=> Launching game"
./bitfighter
