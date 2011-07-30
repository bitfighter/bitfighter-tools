#!/bin/bash

# Find out where we are with full path
curr_dir="`dirname $0`"
lib_dir="$curr_dir/lib"

# First kill any instance of the logbot already running
pid="`ps ax |grep BitfighterLogBot.jar | grep java |sed 's/^\s*//' | cut -d' ' -f1`"

kill -9 $pid 2>/dev/null

# Now start the logbot and run in background
java -classpath "$lib_dir"/BitfighterLogBot.jar:"$lib_dir"/commons-lang-2.6.jar:"$lib_dir"/json.jar:"$lib_dir"/pircbot.jar org.bitfighter.logbot.Run &

