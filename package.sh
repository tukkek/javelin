#!/bin/bash -e
JAVA=12
JLINK=build/jdk/$JAVA/linux/bin/jlink

echo "Checking if preferences.properties is modified..."
git diff --exit-code preferences.properties > /dev/null
rm -rf "build/output/"

# version input
version=`git log --oneline -1 --decorate`
read -e -i "$version" -p "Edit version name: " version

# javadoc
if [ `true` ]; then
  echo "Generating Javadoc..."
  if [ -d doc/javadoc ]; then rm -r doc/javadoc; fi
  javadoc -d doc/javadoc/ javelin  -subpackages javelin &>/dev/null
fi

#jlink
function build() {
	system=$1
	output="build/output/${system}/javelin"
	echo "Building Javelin for $system (Java $JAVA)..."
	$JLINK --module-path .:build/jdk/${JAVA}/${system}/jmods --add-modules javelin --output "$output/java"
	cp -r build/launcher/$system/* doc avatars maps monsters.xml preferences.properties README.txt audio $output
	echo $version>$output/doc/VERSION.txt
	pushd $output/.. > /dev/null
	zip "../javelin-${system}.zip" . -r > /dev/null
	popd > /dev/null
	rm -rf "build/output/${system}"
}
build linux
build windows
build mac
