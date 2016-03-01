import sbt._

object ConfigurationFiles {
  def writeConfiguration(shortName: String, app: SubApplication, configurationDirectory: File, buildDirectory: File, variables: Map[String, Object]): Unit = {
    IO.createDirectory(packageConfigurationDirectory(shortName, buildDirectory))
    (configurationDirectory / "shared" / shortName * (ExistsFileFilter -- DirectoryFilter)).get
      .foreach(renderConfigurationFile(shortName, app, buildDirectory, variables))
    // we do this second so that app-specific files override shared files
    (configurationDirectory / app.name / shortName * (ExistsFileFilter -- DirectoryFilter)).get
      .foreach(renderConfigurationFile(shortName, app, buildDirectory, variables))

    val buildImage = buildDirectory / "image"

    val filesToCopy = {
      val sharedFiles =
        Path.allSubpaths(configurationDirectory / "shared" / shortName / "image")
        .map(t => (t._2, t._1)).toMap

      Path.allSubpaths(configurationDirectory / app.name / shortName / "image")
        .foldLeft(sharedFiles) {
          case (allFiles, (file, relPath)) => allFiles + (relPath -> file)
        }
    }

    IO.copy(
      filesToCopy.map {
        case (relPath, srcFile) => (srcFile, buildImage / relPath)
      })
  }

  private def packageConfigurationDirectory(shortName: String, buildDirectory: File): File =
    buildDirectory / "package" / shortName

  private def renderConfigurationFile(shortName: String, app: SubApplication, buildDirectory: File, variables: Map[String, Object])(f: File): Unit = {
    val packageConfigurationDir = packageConfigurationDirectory(shortName, buildDirectory)
    val outputFile = packageConfigurationDir / outputFileName(f, app)
    if (f.getName.endsWith(".mustache"))
      Mustache(f, outputFile, variables)
    else
      IO.copyFile(f, outputFile)
  }

  private def outputFileName(f: File, app: SubApplication): String =
    f.getName.replaceAllLiterally("shared", app.name).stripSuffix(".mustache")
}
