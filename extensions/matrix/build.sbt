import org.nlogo.build.{ ExtensionDocumentationPlugin, NetLogoExtension }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name       := "matrix"
version    := "1.2.1"
isSnapshot := true

netLogoClassManager := "org.nlogo.extensions.matrix.MatrixExtension"
netLogoVersion      := "7.0.0-2486d1e"

scalaVersion        := "3.7.0"
Test / scalaSource := baseDirectory.value / "src" / "test"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "us-ascii", "-release", "11")

Compile / javaSource := baseDirectory.value / "src" / "main"
javacOptions  ++= Seq("-g", "-deprecation", "-encoding", "us-ascii", "--release", "11")

libraryDependencies ++= Seq(
  "gov.nist.math" % "jama" % "1.0.3"
)
