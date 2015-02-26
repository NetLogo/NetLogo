#!/bin/bash -ev

# -e makes the whole thing die with an error if any command does
# -v lets you see the commands as they happen

if [ "$1" == --clean ] ; then
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

./sbt threed fast:test 2>&1 | tee tmp/nightly/2-threed-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: threed fast:test"; exit 1; fi
echo "*** done: threed fast:test"

# This is neede for travis so that it won't run itself out of memory
JVM_OPTS="-Dfile.encoding=UTF8 -XX:MaxPermSize=1024m -Xms512m -Xmx1536m -Xss2m" \
  ./sbt extensions 2>&1 | tee tmp/nightly/extensions.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: extensions"; exit 1; fi
echo "*** done: extensions"

./sbt slow:test 2>&1 | tee tmp/nightly/3-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: slow:test"; exit 1; fi
echo "*** done: slow:test"

./sbt nogen slow:test 2>&1 | tee tmp/nightly/4-nogen-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen slow:test"; exit 1; fi
echo "*** done: nogen slow:test"

./sbt threed slow:test 2>&1 | tee tmp/nightly/5-threed-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: threed slow:test"; exit 1; fi
echo "*** done: threed slow:test"

# This is neede for travis so that it won't run itself out of memory
JVM_OPTS="-Dfile.encoding=UTF8 -XX:MaxPermSize=1024m -Xms512m -Xmx1536m -Xss2m" \
  ./sbt all 2>&1 | tee tmp/nightly/6-sbt-all.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: sbt all"; exit 1; fi
echo "*** done: sbt all"

./sbt depend 2>&1 | tee tmp/nightly/7-depend.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: depend"; exit 1; fi
echo "*** done: depend"

echo "****** all done!"
