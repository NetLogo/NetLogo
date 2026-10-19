@echo off

REM find out what directory we were started from, and save that.
for /f "tokens=*" %%a in ( 'CD' ) do (
set WD=%%a
)

REM go to the "behaviorsearch" folder that this batch file resides in
set BSEARCH_DIR=%~dp0
REM get rid of the trailing backslash, which causes problems...
set BSEARCH_DIR=%BSEARCH_DIR:~0,-1%
cd %BSEARCH_DIR%

REM (BehaviorSearch needs to start in the NetLogo base folder, 
REM and "behaviorsearch" must be installed as a subfolder of the NetLogo folder.)
REM go up a level, to get to the NetLogo folder.
cd ..

REM If you have enough RAM, up the '768m' below to '1536m' or more.
REM More RAM is especially helpful for multiple threads/parallel execution.
REM CAUTION: for some reason if the max memory is set too high on certain Windows
REM machines, then the program won't launch at all!
set BSEARCH_MAXMEM=768m

REM This assumes that java was installed bundled with NetLogo. 
REM If not, you should change the path below for your java installation.
jre\bin\java.exe -Dbsearch.startupfolder="%WD%" -Dbsearch.appfolder="%BSEARCH_DIR%" -server -Xms256m -XX:+UseParallelGC "-Xmx%BSEARCH_MAXMEM%" -jar "%BSEARCH_DIR%/behaviorsearch_gui.jar" %*


