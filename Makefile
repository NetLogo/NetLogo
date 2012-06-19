# comment out if you want, when debugging this
.SILENT:
# sadly this only works on plain files not directories.
.DELETE_ON_ERROR:

### top level targets; "netlogo" is default target.  the "dict.txt" is
### because we also need to generate the "split dictionary.html" files
.PHONY: netlogo sbt
netlogo: bin/Scripting.class resources/system/dict.txt | tmp

### misc variables
ifneq (,$(findstring CYGWIN,$(shell uname -s)))
    COLON = \;
    JAVA_HOME = `cygpath -up "\Java\jdk1.6.0_31"`
else
    COLON = :
    ifneq (,$(findstring Darwin,$(shell uname)))
        JAVA_HOME = `/usr/libexec/java_home -F -v1.6*`
    else
        JAVA_HOME = /usr/lib/jvm/java-6-sun
    endif
endif

# you might want to specify JARGS from the command line - ST 3/14/11
JAVA = $(JAVA_HOME)/bin/java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Xmx1024m -Djava.library.path=./lib -XX:MaxPermSize=128m -Xfuture $(JARGS)
SCALA_VERSION = 2.9.2
SCALA_JAR_BASE = $(HOME)/.sbt/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar
SCALA_JAR := $(SCALA_JAR_BASE)
CLASSES = target/scala-$(SCALA_VERSION)/classes

# note that LIBS has a trailing $(COLON)
ifneq (,$(findstring CYGWIN,$(shell uname -s)))
    SCALA_JAR := `cygpath -w $(SCALA_JAR)`
    CLASSES := `cygpath -w $(CLASSES)`
    LIBS = `find lib_managed -name \*.jar | xargs cygpath -w | perl -pe 's/\n/;/'`
else
    LIBS = `find lib_managed -name \*.jar | perl -pe 's/\n/:/'`
endif

CLASSPATH = $(LIBS)$(CLASSES)$(COLON)resources$(COLON)$(SCALA_JAR)

### common prerequisites
tmp:
	@echo "@@@ making tmp"
	mkdir -p tmp

### sbt
# the extra "clean" here is annoying but on the java5 branch,
# for some reason sbt always thinks dist/java5/classes.jar
# is dirty and attempts to recompile all Scala sources, which
# then fails for some unknown reason having to do with some
# of the build products already being present. so we need to clean
# before generating infotab.html so that the (not actually
# needed) recompile will succeed.  sigh - ST 6/19/12
sbt $(SCALA_JAR) $(JARS) models/index.txt docs/infotab.html libs:
	bin/sbt info-tab clean extensions model-index native-libs

### targets for running
goshell:
	rlwrap $(JAVA) -classpath $(CLASSPATH) org.nlogo.headless.Shell $(ARGS)

# profiling (Java)
.PHONY: profile-bench profile profiles
profile-bench: netlogo
	$(JAVA) -classpath $(CLASSPATH) -Xrunhprof:cpu=samples,depth=40,file=tmp/java.hprof.txt org.nlogo.headless.HeadlessBenchmarker $(ARGS)
	$(JAVA) -Djava.awt.headless=false -jar project/plugins/lib_managed/scala_2.7.7/perfanal-1.0.jar tmp/java.hprof.txt
profile:
	$(JAVA) -Djava.awt.headless=false -jar project/plugins/lib_managed/scala_2.7.7/perfanal-1.0.jar tmp/profiles/$(ARGS).txt
profiles:
	bin/profiles.scala $(ARGS)

# This also generates the docs/dict folder
resources/system/dict.txt: bin/dictsplit.py docs/dictionary.html
	@echo "@@@ building dict.txt"
	@rm -rf docs/dict
	python bin/dictsplit.py

### Scaladoc

# for internal devel team use
tmp/scaladoc: | tmp
	-rm -rf tmp/scaladoc
	-mkdir -p tmp/scaladoc
	-$(JAVA) -cp $(CLASSPATH) org.nlogo.headless.Main --version | sed -e "s/^NetLogo //" > tmp/version.txt
	bin/scaladoc \
	  -d tmp/scaladoc \
	  -doc-title 'NetLogo' \
	  -doc-version `cat tmp/version.txt` \
	  -classpath $(LIBS)$(CLASSES) \
	  -sourcepath src/main \
          -doc-source-url https://github.com/NetLogo/NetLogo/blob/`cat tmp/version.txt`/src/main€{FILE_PATH}.scala \
	  -encoding us-ascii \
          `find src/main -name \*.scala -o -name \*.java`
# compensate for issues.scala-lang.org/browse/SI-5388
	perl -pi -e 's/\.java.scala/.java/g' `find tmp/scaladoc -name \*.html`

# these are the docs we include with the User Manual
docs/scaladoc: | tmp
	echo "classpath =" $(CLASSPATH)
	-rm -rf docs/scaladoc
	-mkdir -p docs/scaladoc
	-$(JAVA) -cp $(CLASSPATH) org.nlogo.headless.Main --version | sed -e "s/^NetLogo //" > tmp/version.txt
	bin/scaladoc \
	  -d docs/scaladoc \
	  -doc-title 'NetLogo API' \
	  -doc-version `cat tmp/version.txt` \
	  -classpath $(LIBS)$(CLASSES) \
	  -sourcepath src/main \
          -doc-source-url https://github.com/NetLogo/NetLogo/blob/`cat tmp/version.txt`/src/main€{FILE_PATH}.scala \
	  -encoding us-ascii \
	  src/main/org/nlogo/app/App.scala \
	  src/main/org/nlogo/lite/InterfaceComponent.scala \
	  src/main/org/nlogo/lite/Applet.scala \
	  src/main/org/nlogo/lite/AppletPanel.scala \
	  src/main/org/nlogo/headless/HeadlessWorkspace.scala \
          src/main/org/nlogo/api/*.*a \
          src/main/org/nlogo/agent/*.*a \
          src/main/org/nlogo/workspace/*.*a \
          src/main/org/nlogo/nvm/*.*a
# compensate for issues.scala-lang.org/browse/SI-5388
	perl -pi -e 's/\.java.scala/.java/g' `find docs/scaladoc -name \*.html`

### misc targets

# benchmarking
.PHONY: bench benches
bench: netlogo
	$(JAVA) -classpath $(CLASSPATH) org.nlogo.headless.HeadlessBenchmarker $(ARGS)
benches: netlogo
	bin/benches.scala $(ARGS) | tee tmp/bench.txt

# Scala scripting library
bin/Scripting.class: bin/Scripting.scala | $(SCALA_JAR)
	@echo "@@@ building bin/Scripting.class"
	cd bin ; JAVA_HOME=$(JAVA_HOME) ../bin/scalac -deprecation Scripting.scala

# count lines of code
.PHONY: cloc
cloc: tmp/cloc.pl
	tmp/cloc.pl \
          --exclude-ext=m,xml,html,css,dtd \
          --exclude-dir=tmp,project/build/classycle,project/plugins/src_managed \
          --progress-rate=0 \
          .
tmp/cloc.pl: | tmp
	curl -sS 'http://ccl.northwestern.edu/devel/cloc-1.53.pl' -o tmp/cloc.pl
	chmod +x tmp/cloc.pl
