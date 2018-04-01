#!/bin/bash

python="/usr/bin/python"

cd ~/apps/bitfighter-logbot/

#$python main.py 1>> logbot.log 2>> logbot.log &
$python main.py 2>&1 | tee -a logbot.log &

