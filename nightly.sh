#!/bin/bash -ev

# -e makes the whole thing die with an error if any command does
# -v lets you see the commands as they happen

make bin/sbt-launch.jar
bin/sbt update
make clean clean-extensions
make

rm -rf tmp/nightly
mkdir -p tmp/nightly

# here we're using pipes so "-e" isn't enough to stop when something fails.
# maybe there's an easier way, than I've done it below, I don't know. 
# I suck at shell scripting - ST 2/15/11
bin/sbt test-fast 2>&1 | tee tmp/nightly/0-test-fast.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: test-fast"; exit 1; fi
echo "*** done: test-fast"
bin/sbt nogen test-fast 2>&1 | tee tmp/nightly/1-nogen-test-fast.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen test-fast"; exit 1; fi
echo "*** done: nogen test-fast"

bin/sbt test-slow 2>&1 | tee tmp/nightly/2-test-slow.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: test-slow"; exit 1; fi
echo "*** done: test-slow"
bin/sbt nogen test-slow 2>&1 | tee tmp/nightly/3-nogen-test-slow.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen test-slow"; exit 1; fi
echo "*** done: nogen test-slow"

bin/sbt depend 2>&1 | tee tmp/nightly/4-depend.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: depend"; exit 1; fi
echo "*** done: depend"
bin/sbt pmd 2>&1 | tee tmp/nightly/5-pmd.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: pmd"; exit 1; fi
echo "*** done: pmd"

echo "****** all done!"
