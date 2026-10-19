import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "profiler"
version := "1.3.2"
isSnapshot := true

scalaVersion := "3.7.0"

Compile / javaSource := baseDirectory.value / "src"
javacOptions ++= Seq("-g", "-encoding", "us-ascii", "--release", "21")
netLogoVersion      := "7.1.0-alpha1"
netLogoClassManager := "org.nlogo.extensions.profiler.ProfilerExtension"
