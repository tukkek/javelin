@echo off
set JLINK_VM_OPTIONS=--enable-native-access=javelin,org.lwjgl -Djava.library.path=native
SET PATH=%PATH%;fmedia\

java\bin\java %JLINK_VM_OPTIONS% -m javelin/javelin.Javelin %*
