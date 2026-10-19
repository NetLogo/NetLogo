import org.nlogo.build.{ ExtensionDocumentationPlugin, NetLogoExtension }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

scalaVersion := "3.7.0"

name       := "LevelSpace"
version    := "2.4.5"
isSnapshot := true

netLogoExtName      := "ls"
netLogoClassManager := "org.nlogo.ls.LevelSpace"
netLogoVersion      := "7.1.0-internal1-0194cd8"
netLogoTestExtras   += (baseDirectory.value / "test")

Compile / scalaSource := baseDirectory.value / "src" / "main"
Test / scalaSource    := baseDirectory.value / "src" / "test"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "us-ascii", "-feature", "-release", "11", "-Wunused:linted")

lazy val osName = (System.getProperty("os.name"), System.getProperty("os.arch")) match {
  case (n, _) if n.startsWith("Linux") => "linux"
  case (n, arch) if n.startsWith("Mac") && arch == "aarch64" => "mac-aarch64"
  case (n, _) if n.startsWith("Mac") => "mac"
  case (n, _) if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "18.0",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
  "org.openjfx" % s"javafx-swing" % "21.0.6" classifier osName
)
