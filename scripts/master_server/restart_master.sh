 #!/bin/bash

dir="/home/master/bitfighter/exe"


# Run this as the correct user
if [ "$USER" != "master" ]; then
	echo "You must be the 'master' user to run the master server"
	exit 1
fi

cd "$dir"

# Kill the master
killall master

process=`pgrep master`

if [ -n "$process" ]; then
	echo "Shutdown failed.  Please kill the master server manually.  Not restarting"
fi

# Run the master.  This should load the INI in the same directory
./master & disown

cd -