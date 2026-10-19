#!/bin/bash

TEMPDIR="`mktemp -d tmp.XXXXXXXXXX`"
# run the TesterTemp protocol, writing output to test1.XXX.csv, using two threads for simultaneous evaluation
# Note: to see all command line usage options, run bsearch_headless.sh without any arguments.
./behaviorsearch-headless.sh -p test_behaviorsearch.bsearch -o "$TEMPDIR/behaviorsearch_test_example_output" -t 2

rm $TEMPDIR/behaviorsearch_test_example_output.*
rmdir $TEMPDIR
echo "If BehaviorSearch is working, you should have seen it count up to 100%."
read -p "Press any key to finish."

