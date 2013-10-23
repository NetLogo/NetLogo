///
/// root project
///

val root = project in file (".") configs(Testing.configs: _*)

///
/// task keys
///

// surely there's some better way to do this - ST 5/30/12
val nogen = taskKey[Unit]("disable bytecode generator")

///
/// ThisBuild -- applies to subprojects too
/// (at the moment we have no subprojects on this branch, but that could change - ST 7/23/13)
///

scalaVersion in ThisBuild := "2.10.3"

scalacOptions in ThisBuild ++=
  "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -target:jvm-1.7 -Xlint -Xfatal-warnings"
  .split(" ").toSeq

javacOptions in ThisBuild ++=
  "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.7 -target 1.7"
  .split(" ").toSeq

// only log problems plz
ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet

// we're not cross-building for different Scala versions
crossPaths in ThisBuild := false

nogen in ThisBuild  := { System.setProperty("org.nlogo.noGenerator", "true") }

// temporarily needed for ScalaTest build which hasn't propagated
// to Maven Central yet - ST 8/14/13
resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies in ThisBuild ++= Seq(
  "asm" % "asm-all" % "3.3.1",
  "org.jmock" % "jmock" % "2.5.1" % "test",
  "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
  "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
  "org.scalatest" %% "scalatest" % "2.0.RC2" % "test"
)

artifactName := { (_, _, _) => "NetLogoHeadless.jar" }

onLoadMessage := ""

resourceDirectory in Compile := baseDirectory.value / "resources"

scalaSource in Compile := baseDirectory.value / "src" / "main"

scalaSource in Test := baseDirectory.value / "src" / "test"

javaSource in Compile := baseDirectory.value / "src" / "main"

javaSource in Test := baseDirectory.value / "src" / "test"

unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"

sourceGenerators in Compile <+= JFlexRunner.task

resourceGenerators in Compile <+= I18n.resourceGeneratorTask

mainClass in Compile := Some("org.nlogo.headless.Main")

Extensions.extensionsTask

val all = taskKey[Unit]("build all the things!!!")

all := { val _ = (
  (packageBin in Compile).value,
  (compile in Test).value,
  Extensions.extensions.value
)}

seq(Testing.settings: _*)

seq(Depend.settings: _*)

seq(Classycle.settings: _*)

seq(Dump.settings: _*)

seq(ChecksumsAndPreviews.settings: _*)

seq(Scaladoc.settings: _*)

org.scalastyle.sbt.ScalastylePlugin.Settings
