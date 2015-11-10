import sbt._
import sbt.complete.Parser, Parser._


import java.io.File

import DistSettings._

val bootCp = System.getProperty("java.home") + "/lib/rt.jar"

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
    ) ++ DistSettings.settings

lazy val macApp = project.in(file("mac-app")).
  settings(sharedAppProjectSettings: _*).
  settings(
    name                                  := "NetLogo-Mac-App",
    artifactPath in Compile in packageBin := target.value / "netlogo-mac-app.jar")

DistSettings.settings

netLogoRoot := baseDirectory.value.getParentFile

// this value is unfortunately dependent upon both the platform and the application
val appMainClass: PartialFunction[(String, String), String] = {
  case ("windows" | "linux", "NetLogo" | "NetLogo 3D" | "NetLogo Logging") => "org.nlogo.app.App"
  case ("macosx",            "NetLogo" | "NetLogo 3D" | "NetLogo Logging") => "org.nlogo.app.MacApplication"
}

lazy val subAppMap: Map[String, SubApplication] =
  Map("NetLogo"         -> NetLogoCoreApp,
      "NetLogo 3D"      -> NetLogoThreeDApp,
      "NetLogo Logging" -> NetLogoLoggingApp)

lazy val platformMap: Map[String, PlatformBuild] =
  Map("linux" -> LinuxPlatform,
      "mac"   -> new MacPlatform(macApp),
      "win"   -> WindowsPlatform)

lazy val appParser: Parser[SubApplication] = subAppMap.map(t => t._1 ^^^ t._2).reduceLeft(_ | _)

lazy val platformParser: Parser[PlatformBuild] = platformMap.map(t => t._1 ^^^ t._2).reduceLeft(_ | _)

lazy val packageApp = inputKey[File]("package specifief app on specified platform")

lazy val packageAction: Def.Initialize[Task[((PlatformBuild, SubApplication)) => Def.Initialize[Task[File]]]] =
  Def.task[((PlatformBuild, SubApplication)) => Def.Initialize[Task[File]]] {
  { (platform: PlatformBuild, app: SubApplication) =>
    Def.bind(platform.dependencyJars) { jarTask =>
      Def.task {
        val jars            = jarTask.value
        val distDir         = baseDirectory.value
        val netLogoDir      = netLogoRoot.value
        val buildDirectory  = target.value / app.name / platform.shortName
        val artifactsDir    = buildDirectory / "out" / "artifacts"
        val outputDirectory = buildDirectory / "target"
        IO.delete(buildDirectory)
        IO.createDirectory(buildDirectory)

        ConfigurationFiles.writeConfiguration(platform, app, distDir / "configuration", buildDirectory)

        def repathFile(originalBase: File)(f: File): File = {
          val Some(relativeFile) = f relativeTo originalBase
          new java.io.File(artifactsDir, relativeFile.getPath)
        }

        val additionalResources = platform.additionalResources(distDir).flatMap {
          case f if f.isDirectory => Seq((f, artifactsDir / f.getName)) ++
            Path.allSubpaths(f).map(t => (t._1, new File(artifactsDir, f.getName + File.separator + t._2)))
          case f                  => Seq((f, artifactsDir / f.getName))
        }

        val copiedBundleFiles: Seq[(File, File)] =
          platform.bundledDirs.flatMap { bd =>
            val files = bd.files(netLogoDir / bd.directoryName)
            files zip files.map(repathFile(netLogoDir))
          }

        val jarMap = {
          val jarsLessDirs = jars.filterNot(_.isDirectory)
          jarsLessDirs zip jarsLessDirs.map(f => artifactsDir / f.getName)
        }

        val copyLogging = (distDir / "netlogo_logging.xml", artifactsDir / "netlogo_logging.xml")

        val allFileCopies: Seq[(File, File)] = jarMap ++ additionalResources ++ copiedBundleFiles :+ copyLogging

        IO.copy(allFileCopies)

        val allFiles: Seq[File] = allFileCopies.map(_._2)

        JavaPackager(appMainClass(platform.shortName, app.name), platform, app,
          srcDir = artifactsDir, srcFiles = allFiles, outDir = outputDirectory, buildDirectory = buildDirectory)
      }
    }
  }.tupled
}

packageApp <<= InputTask.createDyn(Def.setting((s: State) => " " ~> (platformParser <~ " ") ~ appParser))(packageAction)
