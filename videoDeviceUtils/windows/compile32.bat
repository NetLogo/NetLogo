@echo off

if not exist windows-x86 mkdir windows-x86

cl /LD /Fo"windows-x86/" /Fe"windows-x86/" /I "%JAVA_HOME%\include" /I "%JAVA_HOME%\include\win32" videoDeviceUtils.cpp evr.lib mf.lib mfplat.lib ole32.lib
