#!/bin/bash
#
# Script to kill the BitfighterLogBot

logbot_pid=$(ps ax |grep main.py | grep python | sed 's/^[ ]*//' | cut -d' ' -f1 )

kill -9 $logbot_pid
