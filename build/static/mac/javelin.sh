#!/bin/sh
JLINK_VM_OPTIONS="--enable-native-access=javelin,org.lwjgl,steamworks4j -Djava.library.path=native/"
DIR=`dirname $0`
PATH="$PATH:fmedia/"

$DIR/java/bin/java $JLINK_VM_OPTIONS -m javelin/javelin.Javelin $@
