:: launching a batch-file from Steam opens a terminal window till the game is closed
:: the batch is easier to change and debug so for now support both this and javelin.go
@echo off

set PERMISSIONS=--enable-native-access=javelin,org.lwjgl,steamworks4j,org.lwjgl.jawt,org.lwjgl.opengl
set JLINK_VM_OPTIONS=%PERMISSIONS% -Djava.library.path=native\ -Dorg.lwjgl.opengl.explicitInit=true
set PATH=%PATH%;phiola\

:: not needed but documented here as potential fixes for future errors 
:: set JLINK_VM_OPTIONS=%JLINK_VM_OPTIONS% -Dorg.lwjgl.librarypath=native
:: set PATH=%PATH%;native\

java\bin\java %JLINK_VM_OPTIONS% -m javelin/javelin.Javelin %*
