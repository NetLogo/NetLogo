import sbt._
import Keys._
import librarymanagement.ModuleDescriptorConfiguration

object NetLogoBuild {
  lazy val all = TaskKey[Unit]("all", "build everything!!!")

  def cclArtifacts(path: String): String =
    s"https://s3.amazonaws.com/ccl-artifacts/$path"

  val autogenRoot = taskKey[File]("source root for autogeneration files")

  val netlogoVersion = TaskKey[String]("netlogo-version", "from api.Version")

  lazy val buildDate = taskKey[String]("date of build")

  lazy val marketingVersion = settingKey[String]("Version attached to the build for end-user identification")
  lazy val numericMarketingVersion = settingKey[String]("Numeric-only version attached to the build for end-user identification")

  val settings = Seq(
    marketingVersion        := "7.0.0-internal1",
    numericMarketingVersion := "7.0.0",
    buildDate := {
      val dateFormat =
        new java.text.SimpleDateFormat("MMMMMMMMMMMMMMM d, yyyy")
      dateFormat.format(new java.util.Date())
    },
    netlogoVersion := {
      val loader = (Test / testLoader).value
      val klass = loader.loadClass("org.nlogo.api.Version$")
      val version = klass.getField("MODULE$").get(klass)
      klass.getMethod("version")
        .invoke(version).asInstanceOf[String]
        .stripPrefix("NetLogo ")
    })

  def includeInPackaging(project: Project): Seq[Setting[_]] =
    Seq(Compile / sources ++= (project / Compile / sources).value) ++
    Seq(packageBin, packageSrc, packageDoc).flatMap(task => inTask(task)(
      Compile / mappings ++= {
        val existingMappings = (Compile / mappings).value.map(_._2)
        val newMappings = (project / Compile / mappings).value
        newMappings.filterNot(n => existingMappings.contains(n._2))
      })) ++ Seq(
        ivyModule := {
          val is = ivySbt.value
          val newMs =
            moduleSettings.value match {
              case i: ModuleDescriptorConfiguration =>
                val excludedModule = (project / projectID).value
                val newDeps =
                  i.dependencies
                    .filterNot(m => m.name == excludedModule.name && m.organization == excludedModule.organization)
                i.withDependencies(newDeps)
              case other => other
            }
          new is.Module(newMs)
        },
        libraryDependencies ++= (project / libraryDependencies).value)


  def shareSourceDirectory(path: String): Seq[Setting[_]] = {
    Seq(
      Compile / unmanagedSourceDirectories += baseDirectory.value.getParentFile / path / "src" / "main",
      Test / unmanagedSourceDirectories    += baseDirectory.value.getParentFile / path / "src" / "test"
      ) ++ Seq(Compile, Test).map(config =>
        config / unmanagedSources / excludeFilter := {
          (config / unmanagedSources / excludeFilter).value ||
          new SimpleFileFilter({ f =>
            ! f.isDirectory &&
            Path.rebase(baseDirectory.value.getParentFile / path, baseDirectory.value)(f).map(_.exists).getOrElse(false)
          })
        })
  }
}
