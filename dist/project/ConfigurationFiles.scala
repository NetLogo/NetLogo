import sbt._

object ConfigurationFiles {
  def writeConfiguration(platform: PlatformBuild, app: SubApplication, configurationDirectory: File, buildDirectory: File): Unit = {
    IO.createDirectory(packageConfigurationDirectory(platform, buildDirectory))
    (configurationDirectory / "shared" / platform.shortName * ExistsFileFilter).get
      .foreach(renderConfigurationFile(platform, app, buildDirectory))
    // we do this second so that app-specific files override shared files
    (configurationDirectory / app.name / platform.shortName * ExistsFileFilter).get
      .foreach(renderConfigurationFile(platform, app, buildDirectory))
  }

  private def packageConfigurationDirectory(platform: PlatformBuild, buildDirectory: File): File =
    buildDirectory / "package" / platform.shortName

  private def renderConfigurationFile(platform: PlatformBuild, app: SubApplication, buildDirectory: File)(f: File): Unit = {
    val packageConfigurationDir = packageConfigurationDirectory(platform, buildDirectory)
    val outputFile = packageConfigurationDir / outputFileName(f, app)
    val variables = Map("appName" -> app.name, "version" -> "5.2.2")
    if (f.getName.endsWith(".mustache"))
      writeMustacheFile(f, outputFile, variables)
    else
      IO.copyFile(f, outputFile)
  }

  private def outputFileName(f: File, app: SubApplication): String =
    f.getName.replaceAllLiterally("shared", app.name).stripSuffix(".mustache")

  private def writeMustacheFile(sourceFile: File, destFile: File, variables: Map[String, Object]): Unit = {
      import com.github.mustachejava._
      import scala.collection.JavaConverters._

      val mf = new DefaultMustacheFactory()

      val mustache = IO.reader(sourceFile)(mf.compile(_, sourceFile.getName))

      Using.fileWriter()(destFile) { wrtr =>
        println("rendering: " + sourceFile.getName)
        mustache.execute(wrtr, variables.asJava)
      }
  }
}
