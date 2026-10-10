@echo off
set PERMISSIONS=--enable-native-access=javelin,org.lwjgl,steamworks4j,org.lwjgl.jawt,org.lwjgl.opengl
set JLINK_VM_OPTIONS=%PERMISSIONS% -Djava.library.path=native/ -Dorg.lwjgl.opengl.explicitInit=true
SET PATH=%PATH%;fmedia\

java\bin\java %JLINK_VM_OPTIONS% -m javelin/javelin.Javelin %*
