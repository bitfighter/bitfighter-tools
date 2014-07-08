#!/bin/bash

if [ "x$1" = "x" ]; then
	echo "Start a dedicated server with a specific directory as the"
	echo "rootdatadir.  This will start the server in a loop that will"
	echo "restart it in case of a crash."
	echo ""
	echo "Usage: $0 SERVER_ROOT"
	echo ""
	echo "Example:"
	echo "   $0 plieades"
	exit 1
fi

exe=/home/master/dedicated_servers/bitfighterd.019c
server_root="$1"

# Thanks kaen!
run_server() {
        echo started server with resourcedir: $2
        while [ true ]
        do
		"$exe" -hostaddr any:$1 -rootdatadir "$2" -master 209.148.88.166:25955

		# Because random sleep functions are an important part of every program!
		sleep 5s
        done
}


# Event server
run_server 28006 /home/master/dedicated_servers/$server_root/ &
disown %1
sleep 5s && wait


