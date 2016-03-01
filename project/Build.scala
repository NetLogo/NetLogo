import sbt._
import Keys._

object NetLogoBuild {
  lazy val all = TaskKey[Unit]("all", "build everything!!!")

  def cclArtifacts(path: String): String =
    s"http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/$path"

  val autogenRoot = taskKey[File]("source root for autogeneration files")

  val netlogoVersion = TaskKey[String]("netlogo-version", "from api.Version")

  lazy val buildDate = taskKey[String]("date of build")

  lazy val marketingVersion = taskKey[String]("Version attached to the build for end-user identification")

  lazy val numericMarketingVersion = taskKey[String]("Numeric-only version attached to the build for end-user identification")

  val settings = Seq(
    marketingVersion        := "6.0-PREVIEW-2-16",
    numericMarketingVersion := "6.0",
    buildDate := {
      val dateFormat =
        new java.text.SimpleDateFormat("MMMMMMMMMMMMMMM d, yyyy")
      dateFormat.format(new java.util.Date())
    },
    netlogoVersion := {
      (testLoader in Test).value
        .loadClass("org.nlogo.api.Version")
        .getMethod("version")
        .invoke(null).asInstanceOf[String]
        .stripPrefix("NetLogo ")
    })

  def includeProject(project: Project): Seq[Setting[_]] = Seq(
    sources in (Compile, doc)         ++= (sources in (project, Compile)).value,
    unmanagedResourceDirectories in Compile += (baseDirectory in project).value / "resources" / "main"
    ) ++ Seq(packageBin, packageSrc, packageDoc).flatMap(task => inTask(task)(
      mappings in Compile ++= {
        val existingMappings = (mappings in Compile).value.map(_._2)
        val newMappings = (mappings in Compile in project).value
        newMappings.filterNot(n => existingMappings.contains(n._2))
      }))

  def shareSourceDirectory(dir: File): Seq[Setting[_]] = Seq(
    unmanagedSourceDirectories in Compile += dir.getAbsoluteFile / "src" / "main",
    unmanagedSourceDirectories in Test    += dir.getAbsoluteFile / "src" / "test"
    ) ++ Seq(Compile, Test).map(config =>
      excludeFilter in unmanagedSources in config := {
        (excludeFilter in unmanagedSources in config).value ||
        new SimpleFileFilter({ f =>
          ! f.isDirectory &&
          Path.rebase(dir, baseDirectory.value)(f).map(_.exists).getOrElse(false)
        })
      })
}
