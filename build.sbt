scalaVersion := "2.9.2"

name := "NetLogo"

onLoadMessage := ""

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

scalacOptions ++=
  "-deprecation -unchecked -Xfatal-warnings -Xcheckinit -encoding us-ascii"
  .split(" ").toSeq

javacOptions ++=
  "-bootclasspath dist/java5/classes.jar%sdist/java5/ui.jar -g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.5 -target 1.5"
  .format(java.io.File.pathSeparator)
  .split(" ").toSeq

// only log problems plz
ivyLoggingLevel := UpdateLogging.Quiet

// this makes jar-building and script-writing easier
retrieveManaged := true

// we're not cross-building for different Scala versions
crossPaths := false

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

unmanagedSourceDirectories in Test <+= baseDirectory(_ / "src" / "tools")

unmanagedResourceDirectories in Compile <+= baseDirectory { _ / "resources" }

mainClass in (Compile, run) := Some("org.nlogo.app.App")

javaOptions in run ++= (
  if (System.getProperty("os.name").contains("Mac"))
    Seq(
      "-Dapple.awt.graphics.UseQuartz=true",
      "-Dnetlogo.quaqua.laf=ch.randelshofer.quaqua.snowleopard.Quaqua16SnowLeopardLookAndFeel",
      "-Dapple.awt.showGrowBox=true",
      "-Dapple.laf.useScreenMenuBar=true")
  else
    Seq())

fork in (Compile, run) := true

javaHome in (Compile, run) := (
  if (System.getProperty("os.name").contains("Mac"))
    Some(file(Process(Seq("/usr/libexec/java_home", "-v", "1.8")).!!.dropRight(1)))
  else
    None)

mainClass in (Compile, packageBin) := Some("org.nlogo.app.App")

sourceGenerators in Compile <+= EventsGenerator.task

sourceGenerators in Compile <+= JFlexRunner.task

resourceGenerators in Compile <+= I18n.resourceGeneratorTask

Extensions.extensionsTask

InfoTab.infoTabTask

ModelIndex.modelIndexTask

NativeLibs.nativeLibsTask

Depend.dependTask

threed := { System.setProperty("org.nlogo.is3d", "true") }

nogen  := { System.setProperty("org.nlogo.noGenerator", "true") }

libraryDependencies ++= Seq(
  "asm" % "asm-all" % "3.3.1",
  "org.picocontainer" % "picocontainer" % "2.13.6",
  "log4j" % "log4j" % "1.2.16",
  "javax.media" % "jmf" % "2.1.1e",
  "org.pegdown" % "pegdown" % "1.1.0",
  "org.parboiled" % "parboiled-java" % "1.0.2",
  "steveroy" % "mrjadapter" % "1.2"            from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/mrjadapter-1.2.jar",
  "org.jhotdraw" % "jhotdraw" % "6.0b1"        from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/jhotdraw-6.0b1.jar",
  "ch.randelshofer" % "quaqua" % "9.1"       from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/quaqua-9.1.jar",
  "ch.randelshofer" % "swing-layout" % "9.1" from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/swing-layout-9.1.jar",
  "com.jogamp" % "jogl" % "2.3.2"              from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/jogl-all-2.3.2.jar",
  "com.jogamp" % "gluegen-rt" % "2.3.2"        from "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/gluegen-rt-2.3.2.jar",
  "org.jmock" % "jmock" % "2.5.1" % "test",
  "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
  "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scalatest" %% "scalatest" % "1.8" % "test",
  "org.apache.httpcomponents" % "httpclient" % "4.2",
  "org.apache.httpcomponents" % "httpmime" % "4.2",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1"
)

all <<= (baseDirectory, streams) map { (base, s) =>
  s.log.info("making resources/system/dict.txt and docs/dict folder")
  IO.delete(base / "docs" / "dict")
  Process("python bin/dictsplit.py").!!
}

all <<= all.dependsOn(
  packageBin in Test,
  Extensions.extensions,
  NativeLibs.nativeLibs,
  ModelIndex.modelIndex,
  InfoTab.infoTab,
  Scaladoc.docSmaller)
