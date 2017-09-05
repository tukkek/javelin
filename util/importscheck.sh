#!/bin/bash
# Simple utility to show which external imports are being used.
# Will help with transitioning from AWT to JavaFX.
find ../javelin/ ../tyrant/ |  grep ".java" | xargs grep "import "|cut -d':' -f2|grep -v "javelin"|grep -v "tyrant"|grep -v "//"|sort|uniq
