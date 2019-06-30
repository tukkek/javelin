SHELL=/bin/bash
.SILENT:

JAVA=12
JLINK=build/jdk/$(JAVA)/linux/bin/jlink

define jlink
	echo "Building Javelin for $(1) (Java $(JAVA))..."
	$(JLINK) --module-path .:build/jdk/$(JAVA)/$(1)/jmods --add-modules javelin --output "build/output/$(1)/javelin/java"
	cp -r build/launcher/$(1)/* doc avatars maps monsters.xml preferences.properties README.txt audio build/output/$(1)/javelin
	cp /tmp/VERSION.txt build/output/$(1)/javelin/doc/VERSION.txt
	cd build/output/$(1)/;zip -v "../javelin-$(1).zip" . -r > /dev/null
	rm -rf "build/output/$(1)"
endef

default: checkdirty askversion javadoc windows mac linux
	
checkdirty:
	echo "Checking for dirty preferences.properties..."
	git diff --exit-code preferences.properties > /dev/null

clean:
	rm -rf "build/output/"

askversion:
	read -e -i "$(shell git log --oneline -1 --decorate)" -p "Edit release name: " version;echo $$version > /tmp/VERSION.txt
	
javadoc:
	echo "Generating Javadoc..."
	if [ -d doc/javadoc ]; then rm -r doc/javadoc; fi
	javadoc -d doc/javadoc/ javelin  -subpackages javelin &>/dev/null

windows: checkdirty clean askversion
	$(call jlink,windows)

mac: checkdirty clean askversion
	$(call jlink,mac)

linux: checkdirty clean askversion
	$(call jlink,linux)
