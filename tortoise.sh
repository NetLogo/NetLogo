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

./sbt 'test-only *.tortoise.*' 2>&1 | tee tmp/nightly/tortoise.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: test-only tortoise"; exit 1; fi
echo "*** done: test-only tortoise"

./sbt depend 2>&1 | tee tmp/nightly/depend.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: depend"; exit 1; fi
echo "*** done: depend"

./sbt scalastyle 2>&1 | tee tmp/nightly/scalastyle.txt
if [ `wc -l < target/scalastyle-result.xml` -ne 2 ] ; then echo "*** FAILED: scalastyle"; exit 1; fi
echo "*** done: scalastyle"

echo "****** all done!"
