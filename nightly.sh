#!/bin/bash -ev

# -e makes the whole thing die with an error if any command does
# -v lets you see the commands as they happen

if [ "$1" != --noclean ] && [ "$1" != --no-clean ] ; then
  git clean -fdX
  git submodule update --init
  git submodule foreach git clean -fdX
fi

rm -rf tmp/nightly
mkdir -p tmp/nightly

# here we're using pipes so "-e" isn't enough to stop when something fails.
# maybe there's an easier way, than I've done it below, I don't know.
# I suck at shell scripting - ST 2/15/11

./sbt test:compile 2>&1 | tee tmp/nightly/compile.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: test:compile"; exit 1; fi
echo "*** done: test:compile"

./sbt fast:test 2>&1 | tee tmp/nightly/0-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: fast:test"; exit 1; fi
echo "*** done: fast:test"

./sbt nogen fast:test 2>&1 | tee tmp/nightly/1-nogen-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen fast:test"; exit 1; fi
echo "*** done: nogen fast:test"

./sbt all 2>&1 | tee tmp/nightly/2-sbt-all.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: sbt all"; exit 1; fi
echo "*** done: sbt all"

# experimenting hoping that intermittent Travis failures will stop.
# hopefully turning off logBuffered for the slow tests means that we
# won't go 10 minutes without producing any output.  with logBuffered
# off, the output from the different tests gets scrambled together,
# but we can't turn off parallelExecution or the whole thing will
# take more than 50 minutes to run (the Travis limit) - ST 8/29/13

./sbt 'set logBuffered in test := false' slow:test 2>&1 | tee tmp/nightly/3-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: slow:test"; exit 1; fi
echo "*** done: slow:test"

./sbt 'set logBuffered in test := false' nogen slow:test 2>&1 | tee tmp/nightly/4-nogen-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen slow:test"; exit 1; fi
echo "*** done: nogen slow:test"

./sbt depend 2>&1 | tee tmp/nightly/5-depend.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: depend"; exit 1; fi
echo "*** done: depend"

./sbt scalastyle 2>&1 | tee tmp/nightly/6-scalastyle.txt
if [ `wc -l < target/scalastyle-result.xml` -ne 2 ] ; then echo "*** FAILED: scalastyle"; exit 1; fi
echo "*** done: scalastyle"

echo "****** all done!"
