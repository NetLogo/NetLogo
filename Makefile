# comment out if you want, when debugging this
.SILENT:
# sadly this only works on plain files not directories.
.DELETE_ON_ERROR:

### top level targets; "netlogo" is default target
.PHONY: netlogo sbt
netlogo: bin/Scripting.class | tmp

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
SCALA_JAR_BASE = project/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar
SCALA_JAR := $(SCALA_JAR_BASE)
CLASSES = target/scala_$(SCALA_VERSION)/classes

# note that LIBS has a trailing $(COLON)
ifneq (,$(findstring CYGWIN,$(shell uname -s)))
    SCALA_JAR := `cygpath -w $(SCALA_JAR)`
    CLASSES := `cygpath -w $(CLASSES)`
    LIBS = `ls -1 lib_managed/scala_$(SCALA_VERSION)/compile/*.jar | xargs cygpath -w | perl -pe 's/\n/;/'`
else
    LIBS = `ls -1 lib_managed/scala_$(SCALA_VERSION)/compile/*.jar | perl -pe 's/\n/:/'`
endif

CLASSPATH = $(LIBS)$(CLASSES)$(COLON)resources$(COLON)$(SCALA_JAR)

### common prerequisites
tmp:
	@echo "@@@ making tmp"
	mkdir -p tmp

### sbt
sbt $(SCALA_JAR) $(JARS):
	bin/sbt extensions

### targets for running
goshell:
	rlwrap $(JAVA) -classpath $(CLASSPATH) org.nlogo.headless.Shell $(ARGS)

# profiling (Java)
.PHONY: profile-bench profile profiles
profile-bench: netlogo
	$(JAVA) -classpath $(CLASSPATH) -Xrunhprof:cpu=samples,depth=40,file=tmp/java.hprof.txt org.nlogo.headless.HeadlessBenchmarker $(ARGS)
	$(JAVA) -Djava.awt.headless=false -jar project/plugins/lib_managed/scala_2.7.7/perfanal-1.0.jar tmp/java.hprof.txt
profile:
	$(JAVA) -Djava.awt.headless=false -jar project/plugins/lib_managed/scala_1.7.7/perfanal-1.0.jar tmp/profiles/$(ARGS).txt
profiles:
	bin/profiles.scala $(ARGS)

### Scaladoc

# for internal devel team use
tmp/scaladoc: netlogo | tmp
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
