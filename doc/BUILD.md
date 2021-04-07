This document describes the process of building all platform-specific builds of Javelin intended for distribution, from any supported platform. If you only want to modify and play the game, you don't need to read this - any Java IDE should be enough to automatically handle compiling and running the game (Eclipse in particular should work out-of-the-box after importing the project).

This process is mostly automated through Make, with a few manual setup steps. Javelin's Makefile is written for Linux but should work on MacOS (or Windows with MinGW). Like any Makefile, you can run the default action (all targets), or one or more targets as desired (targets are: javadoc windows mac linux).

# Prerequisites

* `git` (and a proper clone of the Javelin repository)

These can be installed on Windows through a full install of [MinGW](http://mingw-w64.org/doku.php):

* `bash` (usually the default terminal for Linux and Mac)
* GNU `make`
* `zip` command-line utility

## [OpenJDK](http://openjdk.java.net/projects/jdk/)

The build script uses jlink to produce one ZIP file for each operating system. To achieve this, you'll need each system JDK as below:

* build/jdk/windows
* build/jdk/linux
* build/jdk/mac
    
If you don't have Java installed on your system or if your installed Java version is not the one you're building for, you may need to edit the JLINK variable inside the Makefile. You can configure it to use any target JDK by setting it to something like "build/jdk/linux/bin/jlink".

Notice that as of Java 12 the macOS OpenJDK package has a slightly different directory structure to Windows and Linux. To work around this issue, extract only the appropriate folder (Home) so that the resulting JDK directory becomes equivalent to the other ones.

## [fmedia](https://github.com/stsaz/fmedia)

fmedia is used to play external sounds on every operating system. The package needs to be unpacked exactly at these locations:

* build/static/windows/fmedia
* build/static/linux/fmedia
* build/static/mac/fmedia
