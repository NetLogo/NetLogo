import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "sound"
version := "1.2.2"
isSnapshot := true

scalaVersion := "3.7.0"

netLogoVersion      := "7.0.0-beta2-7e8f7a4"
netLogoClassManager := "org.nlogo.extensions.sound.SoundExtension"

Compile / javaSource := baseDirectory.value / "src" / "main" / "java"
javacOptions ++= Seq("-g", "-Xlint:deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path", "-encoding", "us-ascii", "--release", "11")
