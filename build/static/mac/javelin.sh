#!/bin/sh
PERMISSIONS="--enable-native-access=javelin,org.lwjgl,steamworks4j,org.lwjgl.jawt,org.lwjgl.opengl"
# TODO? -XstartOnFirstThread -Dsun.java2d.metal=true
JLINK_VM_OPTIONS="$PERMISSIONS -Djava.library.path=native/ -Dorg.lwjgl.opengl.explicitInit=true"
# TODO? DIR="$(cd "$(dirname "$0")" && pwd)"  
DIR=`dirname $0`
PATH="$PATH:fmedia/"

exec $DIR/java/bin/java $JLINK_VM_OPTIONS -m javelin/javelin.Javelin "$@"
