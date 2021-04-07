@echo off
set JLINK_VM_OPTIONS=
SET PATH=%PATH%;fmedia\

java\bin\java %JLINK_VM_OPTIONS% -m javelin/javelin.Javelin %*
