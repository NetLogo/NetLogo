scalaVersion := "2.9.2"

name := "NetLogo-Mac-App"

val bootCp = System.getProperty("java.home") + "/lib/rt.jar"

javacOptions ++=
  s"-bootclasspath $bootCp -deprecation -g -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.6 -target 1.6"
  .split(" ").toSeq

lazy val netLogoRoot = settingKey[File]("Root directory of NetLogo project")

netLogoRoot := {
  baseDirectory.value.getParentFile.getParentFile
}

unmanagedJars in Compile ++= Seq(netLogoRoot.value / "target" / "NetLogo.jar") ++ (netLogoRoot.value / "lib_managed" ** "*.jar").get

artifactPath in Compile in packageBin :=
  target.value / "netlogo-mac-app.jar"

mainClass in Compile in run := Some("org.nlogo.app.MacApplication")

fork in run := true

libraryDependencies += "org.scala-lang" % "scala-library" % "2.9.2"

packageOptions in (Compile, packageBin) += {
  val distClassPath =
    (dependencyClasspath in Runtime).value.files.map(f =>
        "$APPDIR/Java/" + f.getName).mkString(" ")
  import java.util.jar.Attributes.Name._
  Package.ManifestAttributes(
    "Permissions" -> "sandbox",
    "JavaFX-Version" -> "8.0", // this is required for javapackager to determine the main jar
    "Created-By" -> "JavaFX Packager",
    CLASS_PATH.toString -> distClassPath,
    IMPLEMENTATION_VENDOR.toString -> "netlogo",
    IMPLEMENTATION_TITLE.toString -> "NetLogo",
    IMPLEMENTATION_VERSION.toString -> "5.2.2-SNAPSHOT",
    SPECIFICATION_VENDOR.toString -> "netlogo",
    SPECIFICATION_TITLE.toString -> "NetLogo",
    SPECIFICATION_VERSION.toString -> "5.2.2-SNAPSHOT"
  )
}
