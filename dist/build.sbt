import sbt._

import java.io.File

val bootCp = System.getProperty("java.home") + "/lib/rt.jar"

lazy val netLogoRoot = settingKey[File]("Root directory of NetLogo project")

lazy val jfxPackageOptions = taskKey[Package.ManifestAttributes]("Manifest attributes marking package for javapackager")

val sharedAppProjectSettings = Seq(
  fork in run := true,
  javacOptions ++=
    s"-bootclasspath $bootCp -deprecation -g -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.6 -target 1.6"
      .split(" ").toSeq,
      netLogoRoot              := baseDirectory.value.getParentFile.getParentFile,
      scalaVersion             := "2.9.2",
      libraryDependencies      += "org.scala-lang" % "scala-library" % "2.9.2",
      unmanagedJars in Compile += netLogoRoot.value / "target" / "NetLogo.jar",
      unmanagedJars in Compile ++= (netLogoRoot.value / "lib_managed" ** "*.jar").get,
      jfxPackageOptions                        := JavaPackager.jarAttributes((dependencyClasspath in Runtime).value.files),
      packageOptions in (Compile, packageBin) <+= jfxPackageOptions
    )

lazy val macApp = project.in(file("mac-app")).
  settings(sharedAppProjectSettings: _*).
  settings(
    name                                  := "NetLogo-Mac-App",
    artifactPath in Compile in packageBin := target.value / "netlogo-mac-app.jar")

val packageMacApp = taskKey[File]("package mac app")

val packageWinApp = taskKey[File]("package win app")

// build application jar, resources
val buildNetLogo = taskKey[Unit]("build NetLogo")

buildNetLogo := {
  val netLogoDir = baseDirectory.value.getParentFile

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

def packagePlatformApp(pb: PlatformBuild): Def.Initialize[Task[File]] = {
  Def.taskDyn {
    val distDir      = baseDirectory.value
    val netLogoDir   = distDir.getParentFile
    val artifactsDir = distDir / "out" / "artifacts"
    val netLogoJar   = netLogoDir / "NetLogo.jar"
    pb.dependencyJars(netLogoDir).map { jars =>
      val additionalResources = pb.additionalResources(distDir)

      def repathFile(originalBase: File)(f: File): File = {
        val Some(relativeFile) = f relativeTo originalBase
        new java.io.File(artifactsDir / originalBase.getName, relativeFile.getPath)
      }

      val copiedBundleFiles: Seq[(File, File)] =
        pb.bundledDirs.flatMap { bd =>
          val sourceDir = netLogoDir / bd.directoryName
          val files     = bd.files(sourceDir)
          files zip files.map(repathFile(sourceDir))
        }

      val allJars = jars.filterNot(_.isDirectory)

      IO.delete(artifactsDir)
      IO.createDirectory(artifactsDir)
      IO.copy(allJars zip allJars.map(f => artifactsDir / f.getName), overwrite = true)
      IO.copy(copiedBundleFiles)
      additionalResources.foreach {
        case f if f.isDirectory => IO.copyDirectory(f, artifactsDir / f.getName)
        case f if f.isFile      => IO.copyFile(f, artifactsDir / f.getName)
      }

      val allFiles: Seq[String] =
        (allJars ++ additionalResources).map(_.getName) ++ pb.bundledDirs.map(_.directoryName)

      val args = Seq("javapackager", "-deploy",
        "-title", "NetLogo",
        "-name", "NetLogo",
        "-appclass", pb.mainClass,
        "-native", pb.nativeFormat,
        "-outdir", target.value.getAbsolutePath,
        "-outfile", "NetLogo",
        "-verbose",
        "-srcdir", artifactsDir.getAbsolutePath,
        "-srcfiles", allFiles.mkString(File.pathSeparator),
        "-BmainJar=" + pb.mainJarName) ++
      pb.jvmOptions.map(s => "-BjvmOptions=" + s)

      println(args.mkString(" "))
      val ret = Process(args, distDir).!
      if (ret != 0)
        sys.error("packaging failed!")

      target.value / "bundles" / pb.productName
    }
  }
}

packageMacApp <<= packagePlatformApp(new MacPlatform(macApp))

packageWinApp <<= packagePlatformApp(WindowsPlatform)
