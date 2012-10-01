#!/bin/bash

javac=javac

jar=BitfighterLogBot.jar

curr_dir="`dirname $0`"
jar_path=org/bitfighter/logbot
lib_dir="$curr_dir/lib"
src_root="$curr_dir/src"
src_dir="$src_root/$jar_path"

set -x

# Compile the classes
#cd "$src_dir"
$javac -classpath "$lib_dir"/BitfighterLogBot.jar:"$lib_dir"/commons-lang-2.6.jar:"$lib_dir"/json.jar:"$lib_dir"/pircbot.jar:./src "$src_dir"/*.java "$src_dir"/threads/*.java "$src_dir"/socket/*.java

# Build the JAR
jar cf "$lib_dir"/$jar -C "$src_root" "$jar_path" -C "$src_root" "$jar_path/threads" -C "$src_root" "$jar_path/socket"

set +x
