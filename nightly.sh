#!/bin/bash -ev

# -e makes the whole thing die with an error if any command does
# -v lets you see the commands as they happen

if [ "$1" != --noclean ]; then
  git clean -fdX
  git submodule update --init
  git submodule foreach git clean -fdX
fi

make

rm -rf tmp/nightly
mkdir -p tmp/nightly

# here we're using pipes so "-e" isn't enough to stop when something fails.
# maybe there's an easier way, than I've done it below, I don't know. 
# I suck at shell scripting - ST 2/15/11
bin/sbt fast:test 2>&1 | tee tmp/nightly/0-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: fast:test"; exit 1; fi
echo "*** done: fast:test"
bin/sbt nogen fast:test 2>&1 | tee tmp/nightly/1-nogen-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen fast:test"; exit 1; fi
echo "*** done: nogen fast:test"
bin/sbt threed fast:test 2>&1 | tee tmp/nightly/2-threed-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: threed fast:test"; exit 1; fi
echo "*** done: threed fast:test"

bin/sbt slow:test 2>&1 | tee tmp/nightly/3-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: slow:test"; exit 1; fi
echo "*** done: slow:test"
bin/sbt nogen slow:test 2>&1 | tee tmp/nightly/4-nogen-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen slow:test"; exit 1; fi
echo "*** done: nogen slow:test"
bin/sbt threed slow:test 2>&1 | tee tmp/nightly/5-threed-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: threed slow:test"; exit 1; fi
echo "*** done: threed slow:test"

bin/sbt depend 2>&1 | tee tmp/nightly/6-depend.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: depend"; exit 1; fi
echo "*** done: depend"

echo "****** all done!"
