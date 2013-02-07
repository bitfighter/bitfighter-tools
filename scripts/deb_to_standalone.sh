#!/bin/bash
#
# Convert a DEB to a standalone bitfighter install
#
# This will overwrite the output directory if it previously exists!

deb="$1"
data="data.tar.gz"

bindir="usr/games"
datadir="usr/share/games/bitfighter"

# CHANGE ME for 018a when we've stabilized what file we want to trigger standalone
standalone_file="bitfighter.ini"

output="bitfighter"


## Start
ar xv "$deb"
tar xfz "$data"

rm -r "$output"
mkdir -p "$output"

mv "$bindir"/* "$output/"
mv "$datadir"/* "$output/"

touch "$output/$standalone_file"

## Clean-up
