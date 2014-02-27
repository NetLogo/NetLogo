val root = project in file (".") configs(FastMediumSlow.configs: _*)

scalaVersion := "2.10.4-RC3"

mainClass in Compile := Some("org.nlogo.headless.Main")

onLoadMessage := ""

ivyLoggingLevel := UpdateLogging.Quiet

logBuffered in testOnly in Test := false

name := "NetLogoHeadless"

organization := "org.nlogo"

licenses += ("GPL-2.0", url("http://opensource.org/licenses/GPL-2.0"))

// Used by the publish-versioned plugin
isSnapshot := true

version := "5.1.0"

///
/// building
///

scalacOptions ++=
  "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -target:jvm-1.7 -Xlint -Xfatal-warnings"
  .split(" ").toSeq

javacOptions ++=
  "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.7 -target 1.7"
  .split(" ").toSeq

libraryDependencies ++= Seq(
  "asm" % "asm-all" % "3.3.1"
)

libraryDependencies in ThisBuild ++= Seq(
  "org.jmock" % "jmock" % "2.5.1" % "test",
  "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
  "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.3" % "test",
  "org.scalatest" %% "scalatest" % "2.1.0-RC3" % "test"
)

// reflections depends on some extra jars but for some reason we need to
// explicitly list the transitive dependencies
libraryDependencies ++= Seq(
  "org.reflections" % "reflections" % "0.9.9-RC1" % "test",
  "com.google.code.findbugs" % "jsr305" % "2.0.1" % "test",
  "com.google.guava" % "guava" % "12.0"           % "test",
  "org.javassist" % "javassist" % "3.16.1-GA"     % "test",
  "org.slf4j" % "slf4j-nop" % "1.7.5"             % "test"
)

scalaSource in Compile := baseDirectory.value / "src" / "main"

scalaSource in Test := baseDirectory.value / "src" / "test"

javaSource in Compile := baseDirectory.value / "src" / "main"

javaSource in Test := baseDirectory.value / "src" / "test"

resourceDirectory in Compile := baseDirectory.value / "resources" / "main"

resourceDirectory in Test := baseDirectory.value / "resources" / "test"

///
/// packaging and publishing
///

// don't cross-build for different Scala versions
crossPaths := false

publishArtifact in Test := true

///
/// Scaladoc
///

val netlogoVersion = taskKey[String]("from api.Version")

netlogoVersion := {
  (testLoader in Test).value
    .loadClass("org.nlogo.api.Version")
    .getMethod("version")
    .invoke(null).asInstanceOf[String]
    .stripPrefix("NetLogo ")
}

scalacOptions in (Compile, doc) ++= {
  val version = netlogoVersion.value
  Seq("-encoding", "us-ascii") ++
    Opts.doc.title("NetLogo") ++
    Opts.doc.version(version) ++
    Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
                       version + "/src/main€{FILE_PATH}.scala")
}

// compensate for issues.scala-lang.org/browse/SI-5388
doc in Compile := {
  val path = (doc in Compile).value
  for (file <- Process(Seq("find", path.toString, "-name", "*.html")).lines)
    IO.write(
      new File(file),
      IO.read(new File(file)).replaceAll("\\.java\\.scala", ".java"))
  path
}

///
/// plugins
///

org.scalastyle.sbt.ScalastylePlugin.Settings

///
/// get stuff from project/*.scala
///

FastMediumSlow.settings

bintrayPublishSettings

PublishVersioned.settings

bintray.Keys.repository in bintray.Keys.bintray := "NetLogoHeadless"

bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("netlogo")

Depend.settings
