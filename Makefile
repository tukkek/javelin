SHELL=/bin/bash
.SILENT:

define jlink
	echo "Close Eclipse and enter."
	read
	echo "Building Javelin for $(1)..."
	mkdir --parents build/output/$(1)/javelin/
	mvn clean compile jlink:jlink package -Dplatform=$(1)
	cp -r target/maven-jlink/default build/output/$(1)/javelin/java
	cp -r build/static/$(1)/* doc avatars maps monsters.xml preferences.properties README.txt audio build/output/$(1)/javelin
	cp /tmp/VERSION.txt build/output/$(1)/javelin/doc/VERSION.txt
	mv build/output/$(1)/javelin/doc/apidocs/ build/output/$(1)/javelin/doc/javadoc/
	cp --recursive target/native/ build/output/$(1)/javelin/native/
	cp --recursive build/static/steam/ build/output/$(1)/javelin/native/steam/
	echo "Bundling javelin-$(1).zip"
	cd build/output/$(1)/;zip -v "../javelin-$(1).zip" . -r > /dev/null
endef

default: checkdirty askversion javadoc windows mac linux

javadoc:
	echo "Generating Javadoc..."
	if [ -d doc/javadoc ]; then rm -r doc/javadoc; fi
	mvn clean javadoc:javadoc &>/dev/null
	
checkdirty:
	echo "Checking for dirty preferences.properties..."
	git diff --exit-code preferences.properties > /dev/null

clean:
	rm -rf "build/output/"

askversion:
	read -e -i '$(shell git log --oneline -1 --decorate)' -p "Edit release name: " version;echo $$version > /tmp/VERSION.txt

windows: checkdirty clean askversion
	$(call jlink,windows)

mac: checkdirty clean askversion
	$(call jlink,mac)

linux: checkdirty clean askversion
	$(call jlink,linux)
