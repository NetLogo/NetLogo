import sbt._

object ConfigurationFiles {
  def writeConfiguration(platform: PlatformBuild, app: SubApplication, configurationDirectory: File, buildDirectory: File, variables: Map[String, Object]): Unit = {
    IO.createDirectory(packageConfigurationDirectory(platform, buildDirectory))
    (configurationDirectory / "shared" / platform.shortName * (ExistsFileFilter -- DirectoryFilter)).get
      .foreach(renderConfigurationFile(platform, app, buildDirectory, variables))
    // we do this second so that app-specific files override shared files
    (configurationDirectory / app.name / platform.shortName * (ExistsFileFilter -- DirectoryFilter)).get
      .foreach(renderConfigurationFile(platform, app, buildDirectory, variables))

    val buildImage = buildDirectory / "image"

    val filesToCopy = {
      val sharedFiles =
        Path.allSubpaths(configurationDirectory / "shared" / platform.shortName / "image")
        .map(t => (t._2, t._1)).toMap

      Path.allSubpaths(configurationDirectory / app.name / platform.shortName / "image")
        .foldLeft(sharedFiles) {
          case (allFiles, (file, relPath)) => allFiles + (relPath -> file)
        }
    }

    IO.copy(
      filesToCopy.map {
        case (relPath, srcFile) => (srcFile, buildImage / relPath)
      })
  }

  private def packageConfigurationDirectory(platform: PlatformBuild, buildDirectory: File): File =
    buildDirectory / "package" / platform.shortName

  private def renderConfigurationFile(platform: PlatformBuild, app: SubApplication, buildDirectory: File, variables: Map[String, Object])(f: File): Unit = {
    val packageConfigurationDir = packageConfigurationDirectory(platform, buildDirectory)
    val outputFile = packageConfigurationDir / outputFileName(f, app)
    if (f.getName.endsWith(".mustache"))
      Mustache(f, outputFile, variables)
    else
      IO.copyFile(f, outputFile)
  }

  private def outputFileName(f: File, app: SubApplication): String =
    f.getName.replaceAllLiterally("shared", app.name).stripSuffix(".mustache")
}
