scalaVersion := "2.10.0-M4"

name := "NetLogo"

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

// doing without -Xfatal-warnings for now while Scala 2.10 is still baking - ST 6/19/12
scalacOptions ++=
  "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii"
  .split(" ").toSeq

javacOptions ++=
  "-bootclasspath dist/java5/classes.jar:dist/java5/ui.jar -g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.5 -target 1.5"
  .split(" ").toSeq

// this will make jar-building easier
retrieveManaged := true

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

unmanagedSourceDirectories in Test <+= baseDirectory(_ / "src" / "tools")

unmanagedResourceDirectories in Compile <+= baseDirectory { _ / "resources" }

mainClass in (Compile, run) := Some("org.nlogo.app.App")

mainClass in (Compile, packageBin) := Some("org.nlogo.app.App")

sourceGenerators in Compile <+= Autogen.sourceGeneratorTask

resourceGenerators in Compile <+= I18n.resourceGeneratorTask
 
Extensions.extensionsTask

InfoTab.infoTabTask

ModelIndex.modelIndexTask

NativeLibs.nativeLibsTask

run in Compile <<= (run in Compile).dependsOn(
  NativeLibs.nativeLibs,
  ModelIndex.modelIndex,
  InfoTab.infoTab)

Depend.dependTask

threed := { System.setProperty("org.nlogo.is3d", "true") }

nogen  := { System.setProperty("org.nlogo.noGenerator", "true") }

moduleConfigurations += ModuleConfiguration("javax.media", JavaNet2Repository)

libraryDependencies ++= Seq(
  "asm" % "asm-all" % "3.3.1",
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
  "org.scalacheck" % "scalacheck_2.10.0-M4" % "1.9" % "test",
  "org.scalatest" % "scalatest_2.10.0-M4" % "1.9-2.10.0-M4-B1" % "test"
)
