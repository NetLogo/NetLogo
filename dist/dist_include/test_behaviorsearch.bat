@echo off

REM Note: to see all command line usage options, run bsearch_headless.bat without any arguments.

behaviorsearch-headless.bat -p test_behaviorsearch.bsearch -o %TEMP%/behaviorsearch_test_example_output -t 2

IF EXIST %TEMP%/behaviorsearch_test_example_output.finalBests.csv (
ECHO BehaviorSearch appears to be working fine.
) ELSE (
ECHO BehaviorSearch test failed! Output files not created...
)


del %TEMP%/behaviorsearch_test_example_output*


