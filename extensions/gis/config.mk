NAME=gis
JARS=jai_codec-1.1.3.jar jai_core-1.1.3.jar jscience-4.2.jar jts-1.9.jar commons-codec-1.3.jar commons-logging-1.1.jar commons-httpclient-3.0.1.jar
JARSPATH=jai_codec-1.1.3.jar:jai_core-1.1.3.jar:jscience-4.2.jar:jts-1.9.jar:commons-codec-1.3.jar:commons-logging-1.1.jar:commons-httpclient-3.0.1.jar

jai_codec-1.1.3.jar:
	curl -s 'http://ccl.northwestern.edu/devel/jai_codec-1.1.3.jar' -o jai_codec-1.1.3.jar
jai_core-1.1.3.jar:
	curl -s 'http://ccl.northwestern.edu/devel/jai_core-1.1.3.jar' -o jai_core-1.1.3.jar
jscience-4.2.jar:
	curl -s 'http://ccl.northwestern.edu/devel/jscience-4.2.jar' -o jscience-4.2.jar
jts-1.9.jar:
	curl -s 'http://ccl.northwestern.edu/devel/jts-1.9.jar' -o jts-1.9.jar
commons-codec-1.3.jar:
	curl -s 'http://ccl.northwestern.edu/devel/commons-codec-1.3.jar' -o commons-codec-1.3.jar
commons-logging-1.1.jar:
	curl -s 'http://ccl.northwestern.edu/devel/commons-logging-1.1.jar' -o commons-logging-1.1.jar
commons-httpclient-3.0.1.jar:
	curl -s 'http://ccl.northwestern.edu/devel/commons-httpclient-3.0.1.jar' -o commons-httpclient-3.0.1.jar
