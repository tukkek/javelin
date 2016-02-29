#!/bin/bash
# simple assembly system to package a .zip file for distribution
mkdir package
mkdir package/javelin
cp javelin.jar package/javelin
cp -r doc package/javelin
rm package/javelin/doc/.*~
cp -r avatars package/javelin
cp start.bat package/javelin
cp monsters.xml package/javelin
cp preferences.properties package/javelin
cp README.txt package/javelin
rm javelin.zip
cd package
rm -r javelin/doc/javadoc
zip ../javelin.zip javelin -r > /dev/null
cd ..
rm -r package
rm javelin.jar