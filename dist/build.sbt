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

val packageMacThreeDApp = taskKey[File]("package mac 3D app")

val packageWinApp = taskKey[File]("package win app")

val packageWinThreeDApp = taskKey[File]("package win 3D app")

val packageLinuxApp = taskKey[File]("package linux app")

val packageLinuxThreeDApp = taskKey[File]("package linux 3D app")

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

def packagePlatformApp(subApp: SubApplication)(platformBuild: PlatformBuild): Def.Initialize[Task[File]] = {
  Def.taskDyn {
    val distDir        = baseDirectory.value
    val netLogoDir     = distDir.getParentFile
    val netLogoJar     = netLogoDir / "NetLogo.jar"
    val buildDirectory = target.value / subApp.name / platformBuild.shortName

    platformBuild.dependencyJars(netLogoDir).map { jars =>
      IO.delete(buildDirectory)
      IO.createDirectory(buildDirectory)

      val artifactsDir = buildDirectory / "out" / "artifacts"
      val outputDirectory = buildDirectory / "target"
      val packageConfigurationDir = buildDirectory / "package" / platformBuild.shortName

      IO.createDirectory(packageConfigurationDir)

      (distDir / "configuration" / "shared" / platformBuild.shortName * ExistsFileFilter).get.foreach { f =>
        import com.github.mustachejava._
        import scala.collection.JavaConverters._
        val outputFile = packageConfigurationDir / f.getName.replaceAllLiterally("shared", subApp.name).stripSuffix(".mustache")
        if (f.getName.endsWith(".mustache")) {
          val mf = new DefaultMustacheFactory()
          val mustache = IO.reader(f) { rdr =>
            mf.compile(rdr, f.getName)
          }
          val variables = Map("appName" -> subApp.name, "version" -> "5.2.2")
          Using.fileWriter()(outputFile) { wrtr =>
            println("rendering: " + f.getName)
            mustache.execute(wrtr, variables.asJava)
          }
        } else
          IO.copyFile(f, outputFile)
      }

      (distDir / "configuration" / subApp.name / platformBuild.shortName * ExistsFileFilter).get.foreach { f =>
        IO.copyFile(f, packageConfigurationDir / f.getName)
      }

      val additionalResources = platformBuild.additionalResources(distDir)

      def repathFile(originalBase: File)(f: File): File = {
        val Some(relativeFile) = f relativeTo originalBase
        new java.io.File(artifactsDir, relativeFile.getPath)
      }

      val copiedBundleFiles: Seq[(File, File)] =
        platformBuild.bundledDirs.flatMap { bd =>
          val files = bd.files(netLogoDir / bd.directoryName)
          files zip files.map(repathFile(netLogoDir))
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
        (allJars ++ additionalResources).map(_.getName) ++ platformBuild.bundledDirs.map(_.directoryName)

      val args = Seq[String]("javapackager",
        "-deploy", "-verbose",
        "-title",    subApp.name,
        "-name",     subApp.name,
        "-appclass", subApp.mainClass,
        "-native",   platformBuild.nativeFormat,
        "-outdir",   outputDirectory.getAbsolutePath,
        "-outfile",  subApp.name,
        "-srcdir",   artifactsDir.getAbsolutePath,
        "-srcfiles", allFiles.mkString(File.pathSeparator),
        "-BmainJar=" + platformBuild.mainJarName,
        "-BappVersion=5.2.2") ++
      (subApp.jvmOptions ++ platformBuild.jvmOptions).map(s => "-BjvmOptions=" + s)

      println(args.mkString(" "))
      val ret = Process(args, buildDirectory).!
      if (ret != 0)
        sys.error("packaging failed!")

      outputDirectory / platformBuild.productName
    }
  }
}

packageMacApp <<= packagePlatformApp(MacNetLogoApp)(new MacPlatform(macApp))

packageMacThreeDApp <<= packagePlatformApp(MacNetLogoThreeD)(new MacPlatform(macApp))

packageWinApp <<= packagePlatformApp(WinLinuxNetLogoApp)(WindowsPlatform)

packageWinThreeDApp <<= packagePlatformApp(WinLinuxThreeD)(WindowsPlatform)

packageLinuxApp <<= packagePlatformApp(WinLinuxNetLogoApp)(LinuxPlatform)

packageLinuxThreeDApp <<= packagePlatformApp(WinLinuxThreeD)(LinuxPlatform)
