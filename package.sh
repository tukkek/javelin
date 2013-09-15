#!/bin/bash
# simple assembly system to package a .zip file for distribution
mkdir package
mkdir package/Javelin
cp javelin.jar package/Javelin
cp -r doc package/Javelin
rm package/Javelin/doc/.*~
cp -r avatars package/Javelin
cp start.bat package/Javelin
cp monsters.xml package/Javelin
cp preferences.properties package/Javelin
cp README.txt package/Javelin
rm javelin.zip
cd package
zip ../javelin.zip Javelin -r > /dev/null
cd ..
rm -r package
rm javelin.jar