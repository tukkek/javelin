@echo off
set JLINK_VM_OPTIONS=
java\bin\java %JLINK_VM_OPTIONS% -m javelin/javelin.Javelin %*
