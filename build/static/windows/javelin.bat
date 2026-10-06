@echo off
set JLINK_VM_OPTIONS=--enable-native-access=javelin,org.lwjgl,steamworks4j,org.lwjgl.jawt,org.lwjgl.opengl -Djava.library.path=native
SET PATH=%PATH%;fmedia\

java\bin\java %JLINK_VM_OPTIONS% -m javelin/javelin.Javelin %*
