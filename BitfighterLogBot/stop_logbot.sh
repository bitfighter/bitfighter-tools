#!/bin/bash

# Kill any instance of the logbot already running
pid="`ps ax |grep BitfighterLogBot.jar | grep java |sed 's/^\s*//' | cut -d' ' -f1`"

kill -9 $pid 2>/dev/null
