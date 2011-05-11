# comment out if you want, when debugging this
.SILENT:
# sadly this only works on plain files not directories.
.DELETE_ON_ERROR:

### top level targets; "netlogo" is default target.  the "dict.txt" is
### because we also need to generate the "split dictionary.html" files
.PHONY: netlogo
netlogo: resources/system/dict.txt extensions models/index.txt bin/Scripting.class docs/infotab.html | tmp

### misc variables
ifneq (,$(findstring Darwin,$(shell uname)))
JAVA_HOME = `/usr/libexec/java_home -F -v1.6*`
else
JAVA_HOME = /usr/lib/jvm/java-6-sun-1.6.0.25
endif
# you might want to specify JARGS from the command line - ST 3/14/11
JAVA = $(JAVA_HOME)/bin/java -server -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Xmx1024M -Djava.library.path=./lib -XX:MaxPermSize=128m -Xfuture $(JARGS)
SCALA_VERSION = 2.8.1
LIBS = lib_managed/scala_$(SCALA_VERSION)/compile
CLASSPATH = target/scala_$(SCALA_VERSION)/classes:target/scala_$(SCALA_VERSION)/test-classes:resources:project/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar:$(LIBS)/asm-all-3.3.1.jar:$(LIBS)/picocontainer-2.11.1.jar:$(LIBS)/log4j-1.2.16.jar

### common prerequisites
tmp:
	@echo "@@@ making tmp"
	mkdir -p tmp
bin/sbt-launch.jar:
	curl 'http://ccl.northwestern.edu/devel/sbt-launch-0.7.6.RC0.jar' > bin/sbt-launch.jar
project/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar: bin/sbt-launch.jar
	bin/sbt update
	touch project/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar

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

docs/infotab.html: models/Code\ Examples/Info\ Tab\ Example.nlogo
	bin/sbt gen-info-tab-docs

models/index.txt:
	@echo "@@@ building models/index.txt"
	bin/sbt model-index

###
### JAR building
###

JARS = NetLogo.jar NetLogoLite.jar HubNet.jar BehaviorSpace.jar tmp/scala-library-trimmed.jar
.NOTPARALLEL: $(JARS)
$(JARS): project/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar
	bin/sbt alljars


###
### extensions
###

JAVA_EXTENSIONS=\
	extensions/array/array.jar \
	extensions/gis/gis.jar \
	extensions/sample/sample.jar \
	extensions/table/table.jar
SCALA_EXTENSIONS=\
	extensions/sample-scala/sample-scala.jar \
	extensions/test/test.jar
GITHUB_EXTENSIONS=\
	extensions/bitmap/bitmap.jar \
	extensions/gogo/gogo.jar \
	extensions/matrix/matrix.jar \
	extensions/profiler/profiler.jar \
	extensions/sound/sound.jar \
	extensions/qtj/qtj.jar
EXTENSIONS=$(JAVA_EXTENSIONS) $(SCALA_EXTENSIONS) $(GITHUB_EXTENSIONS)

.PHONY: extensions
extensions: $(EXTENSIONS)

JAVA_EXTENSION_MAKEFILES=$(patsubst %,%Makefile,$(foreach foo,$(JAVA_EXTENSIONS),$(dir $(foo))))
SCALA_EXTENSION_MAKEFILES=$(patsubst %,%Makefile,$(foreach foo,$(SCALA_EXTENSIONS),$(dir $(foo))))

$(JAVA_EXTENSION_MAKEFILES): extensions/Makefile-java.mk
	@echo "@@@ building" $@
	cp extensions/Makefile-java.mk $@

$(SCALA_EXTENSION_MAKEFILES): extensions/Makefile-scala.mk
	@echo "@@@ building" $@
	cp extensions/Makefile-scala.mk $@

$(JAVA_EXTENSIONS): $(JAVA_EXTENSION_MAKEFILES) | NetLogo.jar tmp/scala-library-trimmed.jar
	@echo "@@@ building" $(notdir $@)
	cd $(dir $@); JAVA_HOME=$(JAVA_HOME) SCALA_JAR=../../tmp/scala-library-trimmed.jar $(MAKE) -s $(notdir $@)

$(SCALA_EXTENSIONS): $(SCALA_EXTENSION_MAKEFILES) | NetLogo.jar tmp/scala-library-trimmed.jar
	@echo "@@@ building" $(notdir $@)
	cd $(dir $@); JAVA_HOME=$(JAVA_HOME) $(MAKE) -s $(notdir $@)

$(GITHUB_EXTENSIONS): | NetLogo.jar tmp/scala-library-trimmed.jar
	if [ ! -d extensions/bitmap/src ] ; then git clone http://github.com/NetLogo/Bitmap-Extension.git extensions/bitmap ; fi
	if [ ! -d extensions/gogo/src ] ; then git clone http://github.com/NetLogo/GoGo-Extension.git extensions/gogo ; fi
	if [ ! -d extensions/matrix/src ] ; then git clone http://github.com/NetLogo/Matrix-Extension.git extensions/matrix ; fi
	if [ ! -d extensions/profiler/src ] ; then git clone http://github.com/NetLogo/Profiler-Extension.git extensions/profiler ; fi
	if [ ! -d extensions/qtj/src ] ; then git clone http://github.com/NetLogo/QTJ-Extension.git extensions/qtj ; fi
	if [ ! -d extensions/sound/src ] ; then git clone http://github.com/NetLogo/Sound-Extension.git extensions/sound ; fi
	@echo "@@@ building" $(notdir $@)
	cd $(dir $@); JAVA_HOME=$(JAVA_HOME) SCALA_JAR=../../tmp/scala-library-trimmed.jar $(MAKE) -s $(notdir $@)

### misc targets

# cleaning
.PHONY: clean realclean
clean:
	bin/sbt clean
	rm -f bin/*.class devel/depend.ddf
	rm -rf cobertura.ser docs/dict docs/infotab.html resources/system/dict.txt resources/system/dict3d.txt models/index.txt
	rm -f models/under\ development/intro/output.txt models/benchmarks/other/coords.txt
	rm -f $(EXTENSIONS) $(JAVA_EXTENSION_MAKEFILES) $(SCALA_EXTENSION_MAKEFILES)
	rm -rf extensions/*/build extensions/*/classes
	rm -f $(JARS) BehaviorSpace-src.zip test/applet/NetLogoLite.jar test/applet/HubNet.jar
	rm -rf tmp target docs/javadoc
	rm -rf project/plugins/lib_managed project/plugins/project project/plugins/src_managed project/plugins/target
	rm -f resources/*.properties
realclean:
	git clean -fdX

# benchmarking
.PHONY: benches
benches: netlogo
	bin/benches.scala $(ARGS) | tee tmp/bench.txt

### Scala scripting library
bin/Scripting.class: bin/Scripting.scala project/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar
	@echo "@@@ building bin/Scripting.class"
	cd bin ; JAVA_HOME=$(JAVA_HOME) ../bin/scalac -deprecation Scripting.scala
