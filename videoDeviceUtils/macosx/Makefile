aarch64-new:
	mkdir -p macosx-aarch64-new
	g++ -dynamiclib -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/darwin -framework Foundation -framework AVFoundation -DOSX14 videoDeviceUtils.mm -o macosx-aarch64-new/videoDeviceUtils.dylib

aarch64-old:
	mkdir -p macosx-aarch64-old
	g++ -dynamiclib -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/darwin -framework Foundation -framework AVFoundation videoDeviceUtils.mm -o macosx-aarch64-old/videoDeviceUtils.dylib

x86_64-new:
	mkdir -p macosx-x86_64-new
	g++ -dynamiclib -arch x86_64 -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/darwin -framework Foundation -framework AVFoundation -DOSX14 videoDeviceUtils.mm -o macosx-x86_64-new/videoDeviceUtils.dylib

x86_64-old:
	mkdir -p macosx-x86_64-old
	g++ -dynamiclib -arch x86_64 -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/darwin -framework Foundation -framework AVFoundation videoDeviceUtils.mm -o macosx-x86_64-old/videoDeviceUtils.dylib

all: aarch64-new aarch64-old x86_64-new x86_64-old
