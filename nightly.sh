#!/bin/bash -ev

# -e makes the whole thing die with an error if any command does
# -v lets you see the commands as they happen

if [ "$1" == --clean ] ; then
  git clean -fdX
  git submodule update --init
  git submodule foreach git clean -fdX
fi

./sbt all

rm -rf tmp/nightly
mkdir -p tmp/nightly

# it's absurd, but on java5 branch, we need to clean every time
# because of the usual sbt issue with dist/java5/classes.jar - ST 6/19/12

# here we're using pipes so "-e" isn't enough to stop when something fails.
# maybe there's an easier way, than I've done it below, I don't know.
# I suck at shell scripting - ST 2/15/11
./sbt clean fast:test 2>&1 | tee tmp/nightly/0-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: fast:test"; exit 1; fi
echo "*** done: fast:test"
./sbt clean nogen fast:test 2>&1 | tee tmp/nightly/1-nogen-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen fast:test"; exit 1; fi
echo "*** done: nogen fast:test"
./sbt clean threed fast:test 2>&1 | tee tmp/nightly/2-threed-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: threed fast:test"; exit 1; fi
echo "*** done: threed fast:test"

./sbt clean slow:test 2>&1 | tee tmp/nightly/3-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: slow:test"; exit 1; fi
echo "*** done: slow:test"
./sbt clean nogen slow:test 2>&1 | tee tmp/nightly/4-nogen-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen slow:test"; exit 1; fi
echo "*** done: nogen slow:test"
./sbt clean threed slow:test 2>&1 | tee tmp/nightly/5-threed-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: threed slow:test"; exit 1; fi
echo "*** done: threed slow:test"

./sbt clean depend 2>&1 | tee tmp/nightly/6-depend.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: depend"; exit 1; fi
echo "*** done: depend"

echo "****** all done!"
