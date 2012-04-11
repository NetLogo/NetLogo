scalaVersion := "2.9.1"

name := "NetLogo"

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

scalacOptions ++=
  "-deprecation -unchecked -Xfatal-warnings -Xcheckinit -encoding us-ascii"
  .split(" ").toSeq

javacOptions ++=
  "-bootclasspath dist/java5/classes.jar:dist/java5/ui.jar -g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.5 -target 1.5"
  .split(" ").toSeq

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

resolvers += "java.net" at "http://download.java.net/maven/2"

libraryDependencies ++= Seq("asm" % "asm-all" % "3.3.1",
                            "org.picocontainer" % "picocontainer" % "2.13.6",
                            "steveroy" % "mrjadapter" % "1.2" from "http://ccl.northwestern.edu/devel/mrjadapter-1.2.jar",
                            "ch.randelshofer" % "quaqua" % "7.3.4" from "http://ccl.northwestern.edu/devel/quaqua-7.3.4.jar",
                            "ch.randelshofer" % "swing-layout" % "7.3.4" from "http://ccl.northwestern.edu/devel/swing-layout-7.3.4.jar",
                            "org.jmock" % "jmock" % "2.5.1" % "test",
                            "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
                            "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
                            "org.scalacheck" %% "scalacheck" % "1.9" % "test",
                            "org.scalatest" %% "scalatest" % "1.7.1" % "test")

