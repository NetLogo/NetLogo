# comment out if you want, when debugging this
.SILENT:
# sadly this only works on plain files not directories.
.DELETE_ON_ERROR:

### default target
.PHONY: netlogo
netlogo:
	echo "the Makefile is being phased out. do this instead:"
	echo "bin/sbt all"

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

### targets for running
goshell:
	rlwrap $(JAVA) -classpath $(CLASSPATH) org.nlogo.headless.Shell $(ARGS)

# profiling (Java)
.PHONY: profile-bench profile profiles
profile-bench: netlogo
	-mkdir -p tmp
	$(JAVA) -classpath $(CLASSPATH) -Xrunhprof:cpu=samples,depth=40,file=tmp/java.hprof.txt org.nlogo.headless.HeadlessBenchmarker $(ARGS)
	$(JAVA) -Djava.awt.headless=false -jar project/plugins/lib_managed/scala_2.7.7/perfanal-1.0.jar tmp/java.hprof.txt
profile:
	$(JAVA) -Djava.awt.headless=false -jar project/plugins/lib_managed/scala_2.7.7/perfanal-1.0.jar tmp/profiles/$(ARGS).txt
profiles:
	bin/profiles.scala $(ARGS)

### misc targets

# benchmarking
.PHONY: bench benches
bench: netlogo
	$(JAVA) -classpath $(CLASSPATH) org.nlogo.headless.HeadlessBenchmarker $(ARGS)
benches: netlogo
	-mkdir -p tmp
	bin/benches.scala $(ARGS) | tee tmp/bench.txt
