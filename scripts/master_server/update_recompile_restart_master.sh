 #!/bin/bash

dir="/home/master/bitfighter"

# Run this as the correct user
if [ "$USER" != "master" ]; then
	echo "You must be the 'master' user to run this script"
	exit 1
fi


# Update from HG
pushd "$dir"
echo "=> Updating from Google Code"
hg pull -u
popd

# Save old copy of master
pushd "$dir/exe"
echo "=> Backing up master server"
cp master master.orig
popd

# Recompile
pushd "$dir/build"
echo "=> Building master server"
make
popd

# Restart the master
pushd "$dir/exe"

# Kill the master
killall master

process=`pgrep master`

if [ -n "$process" ]; then
	echo "Shutdown failed.  Please kill the master server manually.  Not restarting"
fi

# Run the master.  This should load the INI in the same directory
./master & disown

popd