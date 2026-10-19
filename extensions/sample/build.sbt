enablePlugins(org.nlogo.build.NetLogoExtension)

name := "sample"
version := "1.2.1"
isSnapshot := true

Compile / javaSource := baseDirectory.value / "src" / "main"
javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path", "-encoding", "us-ascii", "--release", "17")

scalaVersion := "3.7.0"
Test / scalaSource := baseDirectory.value / "src" / "test"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-feature", "-encoding", "us-ascii", "-release", "17")

netLogoVersion      := "7.0.4-bfe6fe4"
netLogoClassManager := "SampleExtension"
