ifeq ($(origin JAVA_HOME_64), undefined)
  $(error You must set the JAVA_HOME_64 variable to the path of your 64-bit JDK)
endif

ifeq ($(origin JAVA_HOME_32), undefined)
  $(error You must set the JAVA_HOME_32 variable to the path of your 32-bit JDK)
endif

include64 := ${JAVA_HOME_64}/include/
include32 := ${JAVA_HOME_32}/include/

64:
	mkdir -p linux-amd64
	g++ -shared -fPIC -I $(include64) -I $(include64)/linux videoDeviceUtils.cpp -o linux-amd64/videoDeviceUtils.so

32:
	mkdir -p linux-i386
	g++ -shared -fPIC -m32 -I $(include32) -I $(include32)/linux videoDeviceUtils.cpp -o linux-i386/videoDeviceUtils.so

all: 64 32
