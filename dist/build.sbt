import sbt._

import java.util.jar.Manifest
import java.util.jar.Attributes.Name._

lazy val macApp = project.in(file("mac-app"))

// build application jar
val packageMacApp = taskKey[Unit]("package mac app")

val buildNetLogo = taskKey[Unit]("build NetLogo")


buildNetLogo := {
  val netLogoDir   = baseDirectory.value.getParentFile

  def netLogoCmd(cmd: String): Unit = {
    val res = Process(Seq("./sbt", cmd), netLogoDir).!
    if (res != 0)
      sys.error("netlogo " + cmd + "failed! Aborting.")
  }

  netLogoCmd("package")
  netLogoCmd("extensions")
  netLogoCmd("model-index")
  netLogoCmd("native-libs")
}

packageMacApp := {
  val distDir = baseDirectory.value

  val artifactsDir = distDir / "out" / "artifacts"

  val netLogoDir   = distDir.getParentFile

  val macAppJar    = (packageBin in Compile in macApp).value

  val netLogoJar   = netLogoDir / "NetLogo.jar"

  //val scalaJar     =
  //  file(System.getProperty("user.home") + "/.sbt/boot/scala-2.9.2/lib/scala-library.jar")

  val dependencies = (dependencyClasspath in macApp in Runtime).value.files
    .filterNot(f => f.getName.contains("scalatest") || f.getName.contains("scalacheck") || f.getName.contains("jmock"))


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

  def repathFile(originalBase: File)(f: File): File = {
    val Some(relativeFile) = f relativeTo originalBase
    new java.io.File(artifactsDir / originalBase.getName, relativeFile.getPath)
  }

  val bundledDirs = Seq[BundledDirectory](
    new ExtensionDir(),
    new ModelsDir(),
    new LibDir(),
    new NativesDir())

  val copiedBundleFiles: Seq[(File, File)] = bundledDirs.flatMap { bd =>
    val sourceDir = netLogoDir / bd.directoryName
    val files     = bd.files(sourceDir)
    files zip files.map(repathFile(sourceDir))
  }

  // def libFileTargets = libFiles.map(repathFile(libDir)).map(f => new File(f.getPath.stripSuffix("jnilib") + "dylib"))

  val allJars = (dependencies :+ macAppJar :+ netLogoJar).filterNot(_.isDirectory)

  IO.delete(artifactsDir)
  IO.createDirectory(artifactsDir)
  IO.copy(allJars zip allJars.map(f => artifactsDir / f.getName), overwrite = true)
  IO.copy(copiedBundleFiles)
  additionalResources.foreach {
    case f if f.isDirectory => IO.copyDirectory(f, artifactsDir / f.getName)
    case f if f.isFile      => IO.copyFile(f, artifactsDir / f.getName)
  }

  val allFiles: Seq[String] =
    (allJars ++ additionalResources).map(_.getName) ++ bundledDirs.map(_.directoryName)

  val jvmOptions =
    "-Dapple.awt.graphics.UseQuartz=true -Djava.ext.dirs= -Xmx1024m -Dfile.encoding=UTF-8".split(" ")

  val args = Seq("javapackager", "-deploy",
    "-title", "NetLogo",
    "-name", "NetLogo",
    "-appclass", "org.nlogo.app.MacApplication",
    "-native", "dmg",
    "-outdir", target.value.getAbsolutePath,
    "-outfile", "NetLogo",
    "-verbose",
    "-srcdir", artifactsDir.getAbsolutePath,
    "-srcfiles", allFiles.mkString(":")) ++
      jvmOptions.map(s => "-BjvmOptions=" + s)

  val ret = Process(args, distDir).!
  if (ret != 0)
    sys.error("packaging failed!")
}
