SHELL=/bin/bash
.SILENT:

define flatten
	find $(1) -type l | while read -r link; do \
		target=$$(readlink -f "$$link"); \
		rm "$$link"; \
		cp -r "$$target" "$$link"; \
	done
endef

define jlink
	read -p "TODO do not distribute steam libraries on GIt Hub!"
	echo "Building Javelin for $(1)..."
	mkdir --parents build/output/$(1)/javelin/
	mvn --activate-profiles $(1) compile jlink:jlink package
	cp -r target/maven-jlink/default build/output/$(1)/javelin/java
	cp -r build/static/$(1)/* doc avatars maps monsters.xml preferences.properties README.txt audio build/output/$(1)/javelin
	cp /tmp/VERSION.txt build/output/$(1)/javelin/doc/VERSION.txt 
	#cp --recursive target/native/ build/output/$(1)/javelin/native/
	#cp --recursive build/static/steam/sdk/redistributable_bin/ build/output/$(1)/javelin/native/steam/
	#$(call flatten, build/output/$(1))
	echo "Bundling javelin-$(1).zip"
	cd build/output/$(1)/;zip -v "../javelin-$(1).zip" . -r > /dev/null
endef

default: windows linux

javadoc: clean
	mvn javadoc:javadoc
	mv doc/apidocs/ doc/javadoc/
	
checkdirty:
	echo "Checking for dirty preferences.properties..."
	git diff --exit-code preferences.properties > /dev/null

clean:
	rm -rf "build/output/"
	if [ -d doc/javadoc/ ]; then rm -r doc/javadoc/; fi
	mvn clean
	
askversion:
	read -e -i '$(shell git log --oneline -1 --decorate)' -p "Edit release name: " version;echo $$version > /tmp/VERSION.txt

warn:
	read -p "Close Eclipse or disable building automatically then enter." 

windows: checkdirty warn askversion clean javadoc
	GOOS=windows GOARCH=amd64 go build -trimpath -ldflags="-s -w -H=windowsgui" -o build/static/windows/javelin.exe build/static/windows/javelin.go
	$(call jlink,windows)

mac: checkdirty warn askversion clean javadoc#TODO
	$(call jlink,mac)

linux: checkdirty warn askversion clean javadoc
	$(call jlink,linux)
