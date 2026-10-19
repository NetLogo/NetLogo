import org.nlogo.build.NetLogoExtension

enablePlugins(NetLogoExtension)

name       := "sample-scala"
version    := "1.2.1"
isSnapshot := true

scalaVersion          := "3.7.0"
Compile / scalaSource := baseDirectory.value / "src" / "main"
Test / scalaSource    := baseDirectory.value / "src" / "test"
scalacOptions        ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "us-ascii", "-release", "11")

netLogoClassManager := "org.nlogo.extensions.samplescala.SampleScalaExtension"
netLogoVersion      := "7.0.0-2486d1e"
netLogoZipExtras   ++= Seq(baseDirectory.value / "README.md", baseDirectory.value / "example-models")
