import sbt._
import Keys._
import librarymanagement.ModuleDescriptorConfiguration

object NetLogoBuild {
  lazy val all = TaskKey[Unit]("all", "build everything!!!")

  def cclArtifacts(path: String): String =
    s"http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/$path"

  val autogenRoot = taskKey[File]("source root for autogeneration files")

  val netlogoVersion = TaskKey[String]("netlogo-version", "from api.Version")

  lazy val buildDate = taskKey[String]("date of build")

  lazy val marketingVersion = settingKey[String]("Version attached to the build for end-user identification")
  lazy val numericMarketingVersion = settingKey[String]("Numeric-only version attached to the build for end-user identification")

  val settings = Seq(
    marketingVersion        := "6.0.3",
    numericMarketingVersion := "6.0.3",
    buildDate := {
      val dateFormat =
        new java.text.SimpleDateFormat("MMMMMMMMMMMMMMM d, yyyy")
      dateFormat.format(new java.util.Date())
    },
    netlogoVersion := {
      val loader = (testLoader in Test).value
      val klass = loader.loadClass("org.nlogo.api.Version$")
      val version = klass.getField("MODULE$").get(klass)
      klass.getMethod("version")
        .invoke(version).asInstanceOf[String]
        .stripPrefix("NetLogo ")
    })

  def includeInPackaging(project: Project): Seq[Setting[_]] =
    Seq(packageBin, packageSrc, packageDoc).flatMap(task => inTask(task)(
      mappings in Compile ++= {
        val existingMappings = (mappings in Compile).value.map(_._2)
        val newMappings = (mappings in Compile in project).value
        newMappings.filterNot(n => existingMappings.contains(n._2))
      })) ++ Seq(
        ivyModule := {
          val is = ivySbt.value
          val newMs =
            moduleSettings.value match {
              case i: ModuleDescriptorConfiguration =>
                val excludedModule = (projectID in project).value
                val newDeps =
                  i.dependencies
                    .filterNot(m => m.name == excludedModule.name && m.organization == excludedModule.organization)
                i.withDependencies(newDeps)
              case other => other
            }
          new is.Module(newMs)
        },
        libraryDependencies ++= (libraryDependencies in project).value)


  def shareSourceDirectory(path: String): Seq[Setting[_]] = {
    Seq(
      unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / path / "src" / "main",
      unmanagedSourceDirectories in Test    += baseDirectory.value.getParentFile / path / "src" / "test"
      ) ++ Seq(Compile, Test).map(config =>
        excludeFilter in unmanagedSources in config := {
          (excludeFilter in unmanagedSources in config).value ||
          new SimpleFileFilter({ f =>
            ! f.isDirectory &&
            Path.rebase(baseDirectory.value.getParentFile / path, baseDirectory.value)(f).map(_.exists).getOrElse(false)
          })
        })
  }
}
