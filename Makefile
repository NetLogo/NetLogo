# comment out if you want, when debugging this
#.SILENT:
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
JAVA_HOME = /usr/lib/jvm/java-6-sun-1.6.0.26
endif
# you might want to specify JARGS from the command line - ST 3/14/11
JAVA = $(JAVA_HOME)/bin/java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Xss16m -Xmx1024m -Djava.library.path=./lib -XX:MaxPermSize=128m -Xfuture $(JARGS)
SCALA_VERSION = 2.9.1
SCALA_JAR = project/boot/scala-$(SCALA_VERSION)/lib/scala-library.jar
# note that LIBS has a trailing colon
LIBS = `ls -1 lib_managed/scala_$(SCALA_VERSION)/compile/*.jar | perl -pe 's/\n/:/'`
CLASSES = target/scala_$(SCALA_VERSION)/classes
CLASSPATH = $(LIBS)$(CLASSES):resources:$(SCALA_JAR)

### common prerequisites
tmp:
	@echo "@@@ making tmp"
	mkdir -p tmp
bin/sbt-launch.jar:
	curl -s 'http://simple-build-tool.googlecode.com/files/sbt-launch-0.7.7.jar' -o bin/sbt-launch.jar
$(SCALA_JAR): | bin/sbt-launch.jar
	bin/sbt update

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

### JAR building

JARS = NetLogo.jar NetLogoLite.jar HubNet.jar
.NOTPARALLEL: $(JARS)
$(JARS): | $(SCALA_JAR)
	bin/sbt alljars

# experimenting with making jars for Java Web Start - ST 10/11/11
$(HOME)/.keystore:
	keytool -genkeypair
tmp/signedjars.zip: $(HOME)/.keystore $(JARS)
	-rm -rf tmp/signedjars*
	-mkdir -p tmp/signedjars
	cp NetLogo.jar $(SCALA_JAR) lib_managed/scala_$(SCALA_VERSION)/compile/*.jar tmp/signedjars
	cp project/build/proguard/manifest.txt tmp/signedjars
        # seems likely the lib/ thing would confuse Web Start - ST 10/11/11
	perl -pi -e "s@lib/@@g" tmp/signedjars/manifest.txt
	jar umf tmp/signedjars/manifest.txt tmp/signedjars/NetLogo.jar
	rm tmp/signedjars/manifest.txt
	for jar in tmp/signedjars/*.jar; do jarsigner -keystore $(HOME)/.keystore -storepass bamsix40 -keypass bamsix40 $$jar mykey; done
	(cd tmp; zip -0 -r signedjars signedjars)
tmp/unsignedjars.zip: $(JARS)
	-rm -rf tmp/unsignedjars*
	-mkdir -p tmp/unsignedjars
	cp NetLogo.jar $(SCALA_JAR) lib_managed/scala_$(SCALA_VERSION)/compile/*.jar tmp/unsignedjars
	cp project/build/proguard/manifest.txt tmp/unsignedjars
        # seems likely the lib/ thing would confuse Web Start - ST 10/11/11
	perl -pi -e "s@lib/@@g" tmp/unsignedjars/manifest.txt
	jar umf tmp/unsignedjars/manifest.txt tmp/unsignedjars/NetLogo.jar
	rm tmp/unsignedjars/manifest.txt
	(cd tmp; zip -0 -r unsignedjars unsignedjars)

### extensions

EXTENSIONS=\
	extensions/array/array.jar \
	extensions/bitmap/bitmap.jar \
	extensions/gis/gis.jar \
	extensions/gogo/gogo.jar \
	extensions/matrix/matrix.jar \
	extensions/network/network.jar \
	extensions/profiler/profiler.jar \
	extensions/sample/sample.jar \
	extensions/sample-scala/sample-scala.jar \
	extensions/sound/sound.jar \
	extensions/table/table.jar \
	extensions/qtj/qtj.jar

.PHONY: extensions
extensions: $(EXTENSIONS)

# most of them use NetLogoLite.jar, but the profiler extension uses NetLogo.jar - ST 5/11/11
$(EXTENSIONS): | NetLogo.jar NetLogoLite.jar
	mkdir -p models
	if [ ! -d models/test ] ; then git clone git://git.assembla.com/models.git ; fi
	mkdir -p extensions
	if [ ! -d extensions/array/src ] ; then git clone http://github.com/NetLogo/Array-Extension.git extensions/array ; fi
	if [ ! -d extensions/bitmap/src ] ; then git clone http://github.com/NetLogo/Bitmap-Extension.git extensions/bitmap ; fi
	if [ ! -d extensions/gis/src ] ; then git clone http://github.com/NetLogo/GIS-Extension.git extensions/gis ; fi
	if [ ! -d extensions/gogo/src ] ; then git clone http://github.com/NetLogo/GoGo-Extension.git extensions/gogo ; fi
	if [ ! -d extensions/matrix/src ] ; then git clone http://github.com/NetLogo/Matrix-Extension.git extensions/matrix ; fi
	if [ ! -d extensions/network/src ] ; then git clone http://github.com/NetLogo/Network-Extension.git extensions/network ; fi
	if [ ! -d extensions/profiler/src ] ; then git clone http://github.com/NetLogo/Profiler-Extension.git extensions/profiler ; fi
	if [ ! -d extensions/qtj/src ] ; then git clone http://github.com/NetLogo/QTJ-Extension.git extensions/qtj ; fi
	if [ ! -d extensions/sample/src ] ; then git clone http://github.com/NetLogo/Sample-Extension.git extensions/sample ; fi
	if [ ! -d extensions/sample-scala/src ] ; then git clone http://github.com/NetLogo/Sample-Scala-Extension.git extensions/sample-scala ; fi
	if [ ! -d extensions/sound/src ] ; then git clone http://github.com/NetLogo/Sound-Extension.git extensions/sound ; fi
	if [ ! -d extensions/table/src ] ; then git clone http://github.com/NetLogo/Table-Extension.git extensions/table ; fi
	@echo "@@@ building" $(notdir $@)
	cd $(dir $@); JAVA_HOME=$(JAVA_HOME) SCALA_JAR=../../$(SCALA_JAR) make -s $(notdir $@)

# pull down versions core devel has rights to push to - ST 5/12/11
.PHONY: repos
repos:
	mkdir -p models
	if [ ! -d models/test ] ; then git clone git@git.assembla.com:models.git ; fi
	mkdir -p extensions
	if [ ! -d extensions/array/src ] ; then git clone git@github.com:/NetLogo/Array-Extension.git extensions/array ; fi
	cd extensions/array; git pull; git status
	if [ ! -d extensions/bitmap/src ] ; then git clone git@github.com:/NetLogo/Bitmap-Extension.git extensions/bitmap ; fi
	cd extensions/bitmap; git pull; git status
	if [ ! -d extensions/gis/src ] ; then git clone git@github.com:/NetLogo/GIS-Extension.git extensions/gis ; fi
	cd extensions/gis; git pull; git status
	if [ ! -d extensions/gogo/src ] ; then git clone git@github.com:/NetLogo/GoGo-Extension.git extensions/gogo ; fi
	cd extensions/gogo; git pull; git status
	if [ ! -d extensions/matrix/src ] ; then git clone git@github.com:/NetLogo/Matrix-Extension.git extensions/matrix ; fi
	cd extensions/matrix; git pull; git status
	if [ ! -d extensions/network/src ] ; then git clone git@github.com:/NetLogo/Network-Extension.git extensions/network ; fi
	cd extensions/network; git pull; git status
	if [ ! -d extensions/profiler/src ] ; then git clone git@github.com:/NetLogo/Profiler-Extension.git extensions/profiler ; fi
	cd extensions/profiler; git pull; git status
	if [ ! -d extensions/qtj/src ] ; then git clone git@github.com:/NetLogo/QTJ-Extension.git extensions/qtj ; fi
	cd extensions/qtj; git pull; git status
	if [ ! -d extensions/sample/src ] ; then git clone git@github.com:/NetLogo/Sample-Extension.git extensions/sample ; fi
	cd extensions/sample; git pull; git status
	if [ ! -d extensions/sample-scala/src ] ; then git clone git@github.com:/NetLogo/Sample-Scala-Extension.git extensions/sample-scala ; fi
	cd extensions/sample-scala; git pull; git status
	if [ ! -d extensions/sound/src ] ; then git clone git@github.com:/NetLogo/Sound-Extension.git extensions/sound ; fi
	cd extensions/sound; git pull; git status
	if [ ! -d extensions/table/src ] ; then git clone git@github.com:/NetLogo/Table-Extension.git extensions/table ; fi
	cd extensions/table; git pull; git status

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
	  -encoding us-ascii \
          `find src/main -name \*.scala -o -name \*.java`

# these are the docs we include with the User Manual
docs/scaladoc: netlogo
	-rm -rf docs/scaladoc
	-mkdir -p docs/scaladoc
	-$(JAVA) -cp $(CLASSPATH) org.nlogo.headless.Main --version | sed -e "s/^NetLogo //" > tmp/version.txt
	bin/scaladoc \
	  -d docs/scaladoc \
	  -doc-title 'NetLogo API' \
	  -doc-version `cat tmp/version.txt` \
	  -classpath $(LIBS)$(CLASSES) \
	  -sourcepath src/main \
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

### misc targets

# cleaning
.PHONY: clean clean-extensions distclean
clean:
	bin/sbt clean
	rm -f bin/*.class devel/depend.ddf
	rm -rf cobertura.ser docs/dict docs/infotab.html resources/system/dict.txt resources/system/dict3d.txt models/index.txt
	rm -rf $(EXTENSIONS) extensions/*/build extensions/*/classes plugins/*/build plugins/*/classes
	rm -f $(JARS) test/applet/NetLogoLite.jar test/applet/HubNet.jar
	rm -rf tmp target docs/scaladoc
	rm -rf project/plugins/lib_managed project/plugins/project project/plugins/src_managed project/plugins/target
	rm -f resources/*.properties
clean-extensions:
	rm -rf $(foreach foo,$(EXTENSIONS),$(dir $(foo)))
distclean:
	git clean -fd
	git clean -fX

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
	curl -s 'http://ccl.northwestern.edu/devel/cloc-1.53.pl' -o tmp/cloc.pl
	chmod +x tmp/cloc.pl
