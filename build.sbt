scalaVersion := "2.9.0-1"

name := "NetLogo"

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
	      	      "-encoding", "us-ascii")

resolvers += "java.net" at "http://download.java.net/maven/2"

libraryDependencies += "asm" % "asm-all" % "3.3.1"

libraryDependencies += "org.picocontainer" % "picocontainer" % "2.11.1"

libraryDependencies += "log4j" % "log4j" % "1.2.16"

libraryDependencies += "javax.media" % "jmf" % "2.1.1e"

libraryDependencies += "org.pegdown" % "pegdown" % "0.9.1"

libraryDependencies += "org.parboiled" % "parboiled-java" % "0.11.0"

libraryDependencies += "steveroy" % "mrjadapter" % "1.2" from
  "http://ccl.northwestern.edu/devel/mrjadapter-1.2.jar"

libraryDependencies += "org.jhotdraw" % "jhotdraw" % "6.0b1" from
    "http://ccl.northwestern.edu/devel/jhotdraw-6.0b1.jar"

libraryDependencies += "ch.randelshofer" % "quaqua" % "7.3.4" from
    "http://ccl.northwestern.edu/devel/quaqua-7.3.4.jar"

libraryDependencies += "org.jogl" % "jogl" % "1.1.1" from
    "http://ccl.northwestern.edu/devel/jogl-1.1.1.jar"

libraryDependencies += "org.gluegen-rt" % "gluegen-rt" % "1.1.1" from
    "http://ccl.northwestern.edu/devel/gluegen-rt-1.1.1.jar"

libraryDependencies += "ch.randelshofer" % "swing-layout" % "7.3.4" from
    "http://ccl.northwestern.edu/devel/swing-layout-7.3.4.jar"

libraryDependencies += "org.jmock" % "jmock" % "2.5.1" % "test"

libraryDependencies += "org.jmock" % "jmock-legacy" % "2.5.1" % "test"

libraryDependencies += "org.scala-tools.testing" % "scalacheck_2.9.0" % "1.9" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test"
