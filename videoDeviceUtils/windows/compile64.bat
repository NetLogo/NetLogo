@echo off

if not exist windows-amd64 mkdir windows-amd64

cl /LD /Fo"windows-amd64/" /Fe"windows-amd64/" /I "%JAVA_HOME%\include" /I "%JAVA_HOME%\include\win32" videoDeviceUtils.cpp evr.lib mf.lib mfplat.lib ole32.lib
