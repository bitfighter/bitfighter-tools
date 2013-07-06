#!/bin/bash

python="/usr/bin/python"

cd ~/apps/bitfighter-logbot/

$python main.py 1>> logbot.log 2>> logbot.log &

