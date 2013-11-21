val root = project in file (".") configs(Testing.configs: _*)

scalaVersion := "2.10.3"

mainClass in Compile := Some("org.nlogo.headless.Main")

onLoadMessage := ""

ivyLoggingLevel := UpdateLogging.Quiet

/// building

scalacOptions ++=
  "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -target:jvm-1.7 -Xlint -Xfatal-warnings"
  .split(" ").toSeq

javacOptions ++=
  "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.7 -target 1.7"
  .split(" ").toSeq

libraryDependencies ++= Seq(
  "asm" % "asm-all" % "3.3.1",
  "org.jmock" % "jmock" % "2.5.1" % "test",
  "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
  "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)

scalaSource in Compile := baseDirectory.value / "src" / "main"

scalaSource in Test := baseDirectory.value / "src" / "test"

javaSource in Compile := baseDirectory.value / "src" / "main"

javaSource in Test := baseDirectory.value / "src" / "test"

unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"

/// packaging and publishing

// don't cross-build for different Scala versions
crossPaths := false

artifactName := { (_, _, _) => "NetLogoHeadless.jar" }

artifactName in Test := { (_, _, _) => "NetLogoHeadlessTests.jar" }

publishArtifact in Test := true

// In English: Put the 'test' dir into 'NetLogoHeadlessTests.jar' at the path
// 'test' --JAB (11/13/13)
mappings in (Test, packageBin) ++= {
  val testDir = baseDirectory.value / "test"
  (testDir.*** --- testDir) x relativeTo(testDir) map {
    case (file, relativePath) => file -> s"test/$relativePath"
  }
}

/// plugins

org.scalastyle.sbt.ScalastylePlugin.Settings

/// get stuff from project/*.scala

Extensions.extensionsTask

Testing.settings

Depend.settings

Scaladoc.settings
