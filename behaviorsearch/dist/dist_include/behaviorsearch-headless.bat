@echo off
setlocal ENABLEDELAYEDEXPANSION

REM find out what directory we were started from, and save that.
for /f "tokens=*" %%a in ( 'CD' ) do (
set WD=%%a
)

REM go to the "behaviorsearch" folder that this batch file resides in
set BSEARCH_DIR=%~dp0
REM get rid of the trailing backslash, which causes problems...
set BSEARCH_DIR=%BSEARCH_DIR:~0,-1%
cd %BSEARCH_DIR%

REM Because of some restrictions in NetLogo (e.g. finding language extensions),
REM BehaviorSearch needs to be started from the NetLogo application folder,
REM and "behaviorsearch" must be installed as a subfolder of the NetLogo folder.)
REM go up a level, to get to the NetLogo folder.
cd ..
set LIBRARY_DIR=%cd%\app

REM gather the classpath composed of all jars
set JARS=
for %%f in (.\*.jar) do (
set JARS=%LIBRARY_DIR%\%%f;!JARS!
)

REM remove trailing semicolon
set JARS=%JARS:~0,-1%

REM If you have enough RAM, up the '768m' below to '1536m' or more.
REM More RAM is especially helpful for multiple threads/parallel execution.
REM CAUTION: for some reason if the max memory is set too high on certain Windows
REM machines, then the program won't launch at all!
set BSEARCH_MAXMEM=768m

REM This script assumes that you have the JAVA_HOME environment variable set.
REM If you don't, and don't want to set it, simply change the path below.
"%JAVA_HOME%\bin\java.exe" -Dbsearch.startupfolder="%WD%" -Dbsearch.appfolder="%BSEARCH_DIR%" -Xms256m -XX:+UseParallelGC "-Xmx%BSEARCH_MAXMEM%" -classpath "%JARS%" bsearch.app.BehaviorSearch %*
