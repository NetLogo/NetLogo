import org.nlogo.build.{ ExtensionDocumentationPlugin, NetLogoExtension }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "view2.5d"
version := "1.2.3"
isSnapshot := true

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path", "-encoding", "us-ascii", "--release", "11")

scalaVersion := "3.7.0"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "us-ascii", "-release", "11")

netLogoVersion      := "7.0.0-2486d1e"
netLogoClassManager := "view25d.View25DExtension"

def cclArtifacts(path: String): String =
  s"https://s3.amazonaws.com/ccl-artifacts/$path"

libraryDependencies ++= Seq(
  "org.jogamp.jogl" % "jogl-all" % "2.4.0" from cclArtifacts("jogl-all-2.4.0.jar")
, "org.jogamp.gluegen" % "gluegen-rt" % "2.4.0" from cclArtifacts("gluegen-rt-2.4.0.jar")
)
