import sbt._

import java.util.jar.Manifest
import java.util.jar.Attributes.Name._

lazy val macApp = project.in(file("mac-app"))

// build application jar
val packageMacApp = taskKey[Unit]("package mac app")

packageMacApp := {
  val distDir = baseDirectory.value

  val artifactsDir = distDir / "out" / "artifacts"

  val netLogoDir   = distDir.getParentFile

  val macAppJar    = (packageBin in Compile in macApp).value

  val netLogoJar   = netLogoDir / "NetLogo.jar"

  def packageProject(base: File): Unit = {
    Process("./sbt package".split(" "), base).!!
  }

  val dependencies = (netLogoDir / "lib_managed" ** "*.jar").get

  val additionalResources: Seq[File] = Seq(
    distDir / "NetLogo.app" / "Contents" / "Resources",
    distDir / "NetLogo.app" / "Contents" / "PkgInfo")

  // Important variables:
  // $APPDIR     - the directory the app is launched from. Contains the cfg file, on Mac Contents/Java
  // $PACKAGEDIR - the package root directory, On Mac Contents
  // $APPDATADIR - platform-specific application data directory
  // $LAUNCHERDIR - same as $PACKAGEDIR on windows, linux. Contents/MacOS on Mac
  //
  // $JREHOME
  // $CACHEDIR

  packageProject(netLogoDir)

  val allJars = (dependencies :+ macAppJar :+ netLogoJar).filterNot(_.isDirectory)

  IO.delete(artifactsDir)
  IO.createDirectory(artifactsDir)
  IO.copy(allJars zip allJars.map(f => artifactsDir / f.getName), overwrite = true)
  additionalResources.foreach {
    case f if f.isDirectory => IO.copyDirectory(f, artifactsDir / f.getName)
    case f if f.isFile      => IO.copyFile(f, artifactsDir / f.getName)
  }
  // IO.copyDirectory(file("extensions"), artifactsDir / "extensions")
  // IO.copyDirectory(file("models"), artifactsDir / "models")

  val allFiles: Seq[String] =
    (allJars ++ additionalResources).map(_.getName)

  val args = Seq("javapackager", "-deploy",
    "-title", "NetLogo",
    "-name", "NetLogo",
    "-appclass", "org.nlogo.app.MacApplication",
    "-native", "dmg",
    "-outdir", target.value.getAbsolutePath,
    "-outfile", "NetLogo",
    "-verbose",
    "-srcdir", artifactsDir.getAbsolutePath,
    "-srcfiles", allFiles.mkString(":"))

  println(args.mkString(" "))

  Process(args, distDir).!!
}
