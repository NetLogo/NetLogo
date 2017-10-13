#!/bin/bash -ev

# -e makes the whole thing die with an error if any command does
# -v lets you see the commands as they happen

if [ "$1" == --clean ] ; then
  git clean -fdX
  git submodule update --init
  git submodule foreach git clean -fdx -e QTJava.jar
fi

./sbt all

rm -rf tmp/nightly
mkdir -p tmp/nightly

./sbt update

# here we're using pipes so "-e" isn't enough to stop when something fails.
# maybe there's an easier way, than I've done it below, I don't know.
# I suck at shell scripting - ST 2/15/11
./sbt test:fast 2>&1 | tee tmp/nightly/0-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: test:fast"; exit 1; fi
echo "*** done: test:fast"
./sbt nogen test:fast 2>&1 | tee tmp/nightly/1-nogen-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen test:fast"; exit 1; fi
echo "*** done: nogen test:fast"

./sbt test:slow 2>&1 | tee tmp/nightly/3-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: test:slow"; exit 1; fi
echo "*** done: test:slow"
./sbt nogen test:slow 2>&1 | tee tmp/nightly/4-nogen-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen test:slow"; exit 1; fi
echo "*** done: nogen test:slow"

./sbt depend 2>&1 | tee tmp/nightly/6-depend.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: depend"; exit 1; fi
echo "*** done: depend"

echo "****** all done!"
