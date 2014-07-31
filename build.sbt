///
/// ThisBuild -- applies to subprojects too
///

scalaVersion in ThisBuild := "2.10.4"

scalacOptions in ThisBuild ++=
  "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -target:jvm-1.6 -Xfatal-warnings -Ywarn-adapted-args -Yinline-warnings"
  .split(" ").toSeq

javacOptions in ThisBuild ++=
  "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.7 -target 1.7"
  .split(" ").toSeq

// only log problems plz
ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet

// this makes jar-building and script-writing easier
retrieveManaged in ThisBuild := true

// we're not cross-building for different Scala versions
crossPaths in ThisBuild := false

threed in ThisBuild := { System.setProperty("org.nlogo.is3d", "true") }

nogen in ThisBuild  := { System.setProperty("org.nlogo.noGenerator", "true") }

libraryDependencies in ThisBuild ++= Seq(
  "asm" % "asm-all" % "3.3.1",
  "org.picocontainer" % "picocontainer" % "2.13.6",
  "org.jmock" % "jmock" % "2.5.1" % "test",
  "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
  "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)

///
/// top-level project only
///

name := "NetLogo"

artifactName := { (_, _, _) => "NetLogo.jar" }

onLoadMessage := ""

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

unmanagedSourceDirectories in Test <+= baseDirectory(_ / "src" / "tools")

unmanagedResourceDirectories in Compile <+= baseDirectory { _ / "resources" }

unmanagedResourceDirectories in Compile <+= baseDirectory { _ / "headless" / "resources" }

mainClass in Compile := Some("org.nlogo.app.App")

sourceGenerators in Compile <+= EventsGenerator.task

Extensions.extensionsTask

InfoTab.infoTabTask

ModelIndex.modelIndexTask

NativeLibs.nativeLibsTask

moduleConfigurations += ModuleConfiguration("javax.media", JavaNet2Repository)

libraryDependencies ++= Seq(
  "log4j" % "log4j" % "1.2.17",
  "javax.media" % "jmf" % "2.1.1e",
  "org.pegdown" % "pegdown" % "1.1.0",
  "org.parboiled" % "parboiled-java" % "1.0.2",
  "steveroy" % "mrjadapter" % "1.2" from "http://ccl.northwestern.edu/devel/mrjadapter-1.2.jar",
  "org.jhotdraw" % "jhotdraw" % "6.0b1" from "http://ccl.northwestern.edu/devel/jhotdraw-6.0b1.jar",
  "ch.randelshofer" % "quaqua" % "7.3.4" from "http://ccl.northwestern.edu/devel/quaqua-7.3.4.jar",
  "ch.randelshofer" % "swing-layout" % "7.3.4" from "http://ccl.northwestern.edu/devel/swing-layout-7.3.4.jar",
  "org.jogamp.jogl" % "jogl-all-main" % "2.1.5-01", // from "http://ccl.northwestern.edu/devel/jogl-2.1.5.jar",
  "org.jogamp.gluegen" % "gluegen-rt-main" % "2.1.5-01", // from "http://ccl.northwestern.edu/devel/gluegen-rt-2.1.5.jar",
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
  packageBin in Compile,
  packageBin in Compile in NetLogoBuild.headless,
  packageBin in Test in NetLogoBuild.headless,
  //Extensions.extensions,
  NativeLibs.nativeLibs,
  ModelIndex.modelIndex,
  InfoTab.infoTab,
  Scaladoc.docSmaller)

///
/// settings from project/*.scala
///

seq(Testing.settings: _*)

seq(Packaging.settings: _*)

seq(Running.settings: _*)

seq(Depend.settings: _*)

seq(Scaladoc.settings: _*)
