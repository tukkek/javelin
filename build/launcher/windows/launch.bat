@echo off
set JLINK_VM_OPTIONS=
set DIR=%~dp0
"%DIR%\java\bin\java" %JLINK_VM_OPTIONS% -m javelin/javelin.Javelin %*
