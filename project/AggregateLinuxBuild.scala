import sbt._
import NetLogoPackaging.RunProcess
import java.io.File

object PackageLinuxAggregate {
  // We're given a dummy package with a directory structure like:
  // dummy
  // ├── dummy (all spaces removed)
  // ├── app
  // │   └── dummy.cfg (all spaces removed)
  // ├── libpackager.so
  // └── runtime
  //     └── JRE
  //
  //  The desire is to create each sub-application, and copy in auxilliary files

  // note: this shares a lot of code with the windows aggregate packager.
  // There may be an opportunity for abstraction.
  private def configureSubApplication(sharedAppRoot: File, app: SubApplication, common: CommonConfiguration, variables: Map[String, AnyRef]): Unit = {
    val allVariables =
      variables ++ app.configurationVariables("linux") +
      ("mainClass"      -> app.mainClass) +
      ("mainClassSlash" -> app.mainClass.replaceAllLiterally(".", "/").replaceAllLiterally("$", "")) +
      ("appIdentifier"  -> app.mainClass.split("\\.").init.mkString(".")) +
      ("classpathJars"  ->
        common.classpath
          .map(_.getName)
          .sorted
          .mkString(File.pathSeparator))

    Mustache(common.configRoot / "shared" / "linux" / "NetLogo.cfg.mustache",
      sharedAppRoot / "app" / (app.name.replaceAllLiterally(" ", "") + ".cfg"), allVariables)

    app.additionalArtifacts(common.configRoot).foreach { f =>
      FileActions.copyFile(f, sharedAppRoot / "app" / f.getName)
    }

  }

  def apply(
    aggregateTarget:        File,
    commonConfig:           CommonConfiguration,
    stubApplicationAndName: (File, String),
    subApplications:        Seq[SubApplication],
    variables:              Map[String, String]): File = {
      import commonConfig.{ jdk, webDirectory }

      val version = variables("version")
      val aggregateLinuxDir = aggregateTarget / s"NetLogo $version"

      IO.delete(aggregateLinuxDir)
      IO.createDirectory(aggregateLinuxDir)

      JavaPackager.copyLinuxStubApplications(
        stubApplicationAndName._1, stubApplicationAndName._2,
        aggregateLinuxDir, subApplications.map(_.name))

      val sharedJars = aggregateLinuxDir / "app"

      commonConfig.bundledDirs.foreach { d =>
        d.fileMappings.foreach {
          case (f, p) =>
            val targetFile = sharedJars / p
            if (! targetFile.getParentFile.isDirectory)
              FileActions.createDirectories(targetFile.getParentFile)
            FileActions.copyFile(f, sharedJars / p)
        }
      }

      commonConfig.classpath.foreach { jar =>
        FileActions.copyFile(jar, sharedJars / jar.getName)
      }

      commonConfig.rootFiles.foreach { f =>
        FileActions.copyAny(f, aggregateLinuxDir / f.getName)
      }

      val variablesAndJarClasspath =

      // configure each sub application
      subApplications.foreach { app =>
        configureSubApplication(aggregateLinuxDir, app, commonConfig, variables)
      }

      val headlessClasspath =
        ("classpathJars"  ->
          commonConfig.classpath
            .map(jar => "app/" + jar.getName)
            .sorted
            .mkString(File.pathSeparator))

      val targetFile = aggregateLinuxDir / "netlogo-headless.sh"
      Mustache(commonConfig.configRoot / "shared" / "linux" / "netlogo-headless.sh.mustache",
        targetFile, variables + headlessClasspath)
      targetFile.setExecutable(true)

      val archiveName = s"NetLogo-$version-${jdk.arch}.tgz"
      val tarBuildDir = aggregateLinuxDir.getParentFile

      RunProcess(Seq("tar", "-zcf", archiveName, aggregateLinuxDir.getName), tarBuildDir, "tar linux aggregate")

      FileActions.createDirectory(webDirectory)
      FileActions.moveFile(tarBuildDir / archiveName, webDirectory / archiveName)

      webDirectory / archiveName
  }
}
