# comment out if you want, when debugging this
.SILENT:
# sadly this only works on plain files not directories.
.DELETE_ON_ERROR:

### misc variables
ifneq (,$(findstring CYGWIN,$(shell uname -s)))
    JAVA_HOME = `cygpath -up "\Java\jdk1.6.0_31"`
else
    ifneq (,$(findstring Darwin,$(shell uname)))
        JAVA_HOME = `/usr/libexec/java_home -F -v1.6*`
    else
        JAVA_HOME = /usr/lib/jvm/java-6-sun
    endif
endif

SCALA_VERSION = 2.9.2
SCALA_JAR = $(HOME)/.sbt/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar

ifneq (,$(findstring CYGWIN,$(shell uname -s)))
    SCALA_JAR := `cygpath -w $(SCALA_JAR)`
endif

.PHONY: netlogo
netlogo bin/Scripting.class: bin/Scripting.scala
	mkdir -p tmp
	bin/sbt extensions
	@echo "@@@ building bin/Scripting.class"
	cd bin ; JAVA_HOME=$(JAVA_HOME) ../bin/scalac -deprecation Scripting.scala
