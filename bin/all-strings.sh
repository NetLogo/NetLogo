#!/bin/bash

grep -rn .*\".*[0-9a-zA-Z][0-9a-zA-Z].*\".* netlogo-gui/src/main | grep -v I18N | grep -v OTPL | grep -v TPL | grep -v "\-TP\-" | grep -v "OT\-\-" | grep -v "//" | grep -v "Version\.java" | grep -v MersenneTwisterFast | grep -v AutoConverter > all-strings.txt
