import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "rnd"
version := "3.1.1"
isSnapshot := true

scalaVersion := "3.7.0"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "UTF8", "-release", "11")

netLogoExtName      := "rnd"
netLogoClassManager := "org.nlogo.extensions.rnd.RndExtension"
netLogoVersion      := "7.0.0-beta2-7e8f7a4"
