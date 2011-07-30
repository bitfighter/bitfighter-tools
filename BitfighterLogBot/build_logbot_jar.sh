#!/bin/bash

jar=BitfighterLogBot.jar

curr_dir="`dirname $0`"
jar_path=org/bitfighter/logbot
lib_dir="$curr_dir/lib"
src_root="$curr_dir/src"
src_dir="$src_root/$jar_path"

set -x

# Compile the classes
#cd "$src_dir"
javac -classpath "$lib_dir"/BitfighterLogBot.jar:"$lib_dir"/commons-lang-2.6.jar:"$lib_dir"/json.jar:"$lib_dir"/pircbot.jar "$src_dir"/*.java

# Build the JAR
jar cf "$lib_dir"/$jar -C "$src_root" "$jar_path"

set +x
