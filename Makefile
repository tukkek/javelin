SHELL=/bin/bash
.SILENT:

JLINK=jlink

define jlink
	echo "Building Javelin for $(1)..."
	$(JLINK) --module-path .:build/jdk/$(1)/jmods --add-modules javelin --output "build/output/$(1)/javelin/java"
	cp -r build/launcher/$(1)/* doc avatars maps monsters.xml preferences.properties README.txt audio build/output/$(1)/javelin
	cp /tmp/VERSION.txt build/output/$(1)/javelin/doc/VERSION.txt
	cd build/output/$(1)/;zip -v "../javelin-$(1).zip" . -r > /dev/null
	rm -rf "build/output/$(1)"
endef

default: checkdirty askversion javadoc windows mac linux

javadoc:
	echo "Generating Javadoc..."
	if [ -d doc/javadoc ]; then rm -r doc/javadoc; fi
	javadoc -d doc/javadoc/ javelin  -subpackages javelin &>/dev/null
	
checkdirty:
	echo "Checking for dirty preferences.properties..."
	git diff --exit-code preferences.properties > /dev/null

clean:
	rm -rf "build/output/"

askversion:
	read -e -i '$(shell git log --oneline -1 --decorate)' -p "Edit release name: " version;echo $$version > /tmp/VERSION.txt

showjavaversion:
	echo "Using jlink: ${shell $(JLINK) --version}"

windows: checkdirty clean askversion showjavaversion
	$(call jlink,windows)

mac: checkdirty clean askversion showjavaversion
	$(call jlink,mac)

linux: checkdirty clean askversion showjavaversion
	$(call jlink,linux)
