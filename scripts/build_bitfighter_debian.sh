#!/bin/sh

# install deps
sudo apt-get --yes install libsdl1.2-dev zlib1g-dev libpng12-dev libopenal-dev libvorbis-dev libspeex-dev libmodplug-dev cmake build-essential mercurial

# checkout latest source code
hg clone https://code.google.com/p/bitfighter bitfighter-hg

# begin building. time to make a sandwhich.
cd bitfighter-hg/build
cmake .. -DCMAKE_BUILD_TYPE=Debug
make -j2

# finally, make some symbolic links so we can launch bitfighter from the exe directory
cd ../exe/
for file in `ls -1 ../resource` ; do ln -s ../resource/$file ; done
./bitfighter
