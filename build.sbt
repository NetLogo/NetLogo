scalaVersion := "2.9.1"

name := "NetLogo"

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

scalacOptions ++=
  "-deprecation -unchecked -Xcheckinit -encoding us-ascii"
  .split(" ").toSeq

javacOptions ++=
  "-g -deprecation -encoding us-ascii -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path"
  .split(" ").toSeq

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

resolvers += "java.net" at "http://download.java.net/maven/2"

libraryDependencies ++= Seq("asm" % "asm-all" % "3.3.1",
                            "org.picocontainer" % "picocontainer" % "2.13.6",
                            "log4j" % "log4j" % "1.2.16",
                            "javax.media" % "jmf" % "2.1.1e",
                            "org.pegdown" % "pegdown" % "1.1.0",
                            "org.parboiled" % "parboiled-java" % "1.0.2",
                            "steveroy" % "mrjadapter" % "1.2" from "http://ccl.northwestern.edu/devel/mrjadapter-1.2.jar",
                            "org.jhotdraw" % "jhotdraw" % "6.0b1" from "http://ccl.northwestern.edu/devel/jhotdraw-6.0b1.jar",
                            "ch.randelshofer" % "quaqua" % "7.3.4" from "http://ccl.northwestern.edu/devel/quaqua-7.3.4.jar",
                            "ch.randelshofer" % "swing-layout" % "7.3.4" from "http://ccl.northwestern.edu/devel/swing-layout-7.3.4.jar",
                            "org.jogl" % "jogl" % "1.1.1" from "http://ccl.northwestern.edu/devel/jogl-1.1.1.jar",
                            "org.gluegen-rt" % "gluegen-rt" % "1.1.1" from "http://ccl.northwestern.edu/devel/gluegen-rt-1.1.1.jar",
                            "org.jmock" % "jmock" % "2.5.1" % "test",
                            "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
                            "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
                            "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9" % "test",
                            "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test")

