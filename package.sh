#!/bin/bash
git diff --exit-code preferences.properties > /dev/null
if [ $? -eq 1 ]; then echo "Unclean preferences file."; exit; fi
rm -rf "build/output/"

# version input
version=`git log --oneline -1 --decorate`
read -e -i "$version" -p "Edit version name: " version

# javadoc
echo "Generating Javadoc..."
rm -r doc/javadoc
javadoc -d doc/javadoc/ javelin  -subpackages javelin &>/dev/null

#jlink
function build() {
    java=$1
    system=$2
    output="build/output/${system}/javelin"
    echo "Building Javelin for $system (Java $java)..."
    jlink --module-path .:build/jdk/${java}/${system}/jmods --add-modules javelin --output "$output/java"
    cp -r build/launcher/$system/* doc avatars maps monsters.xml preferences.properties README.txt audio $output
    echo $version>$output/doc/VERSION.txt
    pushd $output/.. > /dev/null
    zip "../javelin-${system}.zip" . -r > /dev/null
    popd > /dev/null
    rm -rf "build/output/${system}"
}
build "11" "linux"
build "11" "windows"
build "11" "mac"
