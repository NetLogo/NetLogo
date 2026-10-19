import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "csv"
version := "1.2.1"
isSnapshot := true

scalaVersion := "3.7.0"
Compile / scalaSource := baseDirectory.value / "src" / "main"
Test / scalaSource := baseDirectory.value / "src" / "test"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-feature", "-encoding", "us-ascii", "-release", "11")

netLogoVersion      := "7.0.0-beta2-7e8f7a4"
netLogoClassManager := "org.nlogo.extensions.csv.CSVExtension"
netLogoTestExtras += (baseDirectory.value / "test")

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-csv" % "1.0",
  "com.typesafe"       % "config"      % "1.3.1" % "test"
)
