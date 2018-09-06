#!/bin/bash
# simple assembly system to package a .zip file for distribution
version=`git log --oneline -1 --decorate`
read -e -i "$version" -p "Edit version name: " version
ant
mkdir package
mkdir package/javelin
cp javelin.jar package/javelin
cp -r doc package/javelin
rm package/javelin/doc/.*~
echo $version>package/javelin/doc/VERSION.txt
cp -r avatars/ package/javelin
cp -r maps/ package/javelin
cp monsters.xml package/javelin
cp javelin.bat package/javelin
cp preferences.properties package/javelin
cp README.txt package/javelin
rm javelin.zip
cd package
rm -r javelin/doc/javadoc
zip ../javelin.zip javelin -r > /dev/null
cd ..
rm -r package
rm javelin.jar
git diff preferences.properties

