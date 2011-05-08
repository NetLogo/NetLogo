NAME=gogo
JARS=RXTXcomm.jar
JARSPATH=RXTXcomm.jar

RXTXcomm.jar:
	mkdir -p lib/Mac\ OS\ X lib/Windows lib/Linux-x86 lib/Linux-amd64
	curl 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/RXTXcomm.jar' -o RXTXcomm.jar
	curl 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Mac_OS_X/librxtxSerial.jnilib' -o lib/Mac\ OS\ X/librxtxSerial.jnilib
	curl 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Windows/i368-mingw32/rxtxSerial.dll' -o lib/Windows/rxtxSerial.dll
	curl 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Windows/i368-mingw32/rxtxParallel.dll' -o lib/Windows/rxtxParallel.dll
	curl 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Linux/i686-unknown-linux-gnu/librxtxParallel.so' -o lib/Linux-x86/librxtxParallel.so
	curl 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Linux/i686-unknown-linux-gnu/librxtxSerial.so' -o lib/Linux-x86/librxtxSerial.so
	curl 'http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2/Linux/x86_64-unknown-linux-gnu/librxtxSerial.so' -o lib/Linux-amd64/librxtxSerial.so
