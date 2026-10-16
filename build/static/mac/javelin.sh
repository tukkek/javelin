#!/bin/sh
PERMISSIONS="--enable-native-access=javelin,org.lwjgl,steamworks4j,org.lwjgl.jawt,org.lwjgl.opengl"
AWT="-Dsun.java2d.uiScale.enabled=false"
# TODO? -XstartOnFirstThread -Dsun.java2d.metal=true
FLAGS="-Djava.library.path=native/ -Dorg.lwjgl.opengl.explicitInit=true $PERMISSIONS $AWT"
# TODO? DIR="$(cd "$(dirname "$0")" && pwd)"  
DIR=`dirname $0`
PATH="$PATH:phiola/"

exec $DIR/java/bin/java $FLAGS -m javelin/javelin.Javelin "$@"
