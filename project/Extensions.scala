import java.io.File
import sbt._
import Keys._

object Extensions {

  val extensions = TaskKey[Seq[File]]("extensions", "builds extensions")

  val extensionsTask = Seq(
    extensions := {
      val base                   = baseDirectory.value
      val scala                  = scalaInstance.value
      val s                      = streams.value
      val packagedNetLogoJar     = (packageBin in Compile).value
      val packagedNetLogoTestJar = (packageBin in Test).value
      "git submodule --quiet update --init" ! s.log
      val isDirectory = new java.io.FileFilter {
        override def accept(f: File) = f.isDirectory
      }
      val dirs = IO.listFiles(isDirectory)(base / "extensions").toSeq
      val caches = dirs.map{dir =>
        FileFunction.cached(s.cacheDirectory / "extensions" / dir.getName,
                            inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
          in =>
            Set(buildExtension(dir, scala.libraryJar, packagedNetLogoJar, s.log, state.value))
        }}
      caches.flatMap{cache => cache(Set(base / "NetLogo.jar", base / "NetLogoLite.jar"))}
    }
  )

  class NestedConfiguration(val config: xsbti.AppConfiguration, baseDir: File, args: Array[String]) extends xsbti.AppConfiguration {
    override val arguments = args
    override val provider = config.provider
    override val baseDirectory = baseDir
  }

  val runner = new xMain()

  def config(state: State, dir: File, command: String) =
    new NestedConfiguration(state.configuration, dir, Array(command))

  private def buildExtension(dir: File, scalaLibrary: File, netLogoJar: File, log: Logger, state: State): File = {
    log.info("building extension: " + dir.getName)
    System.setProperty("netlogo.jar.url", netLogoJar.toURI.toString)
    val buildConfig  = config(state, dir, "package")
    val jar = dir / (dir.getName + ".jar")
    runner.run(buildConfig) match {
      case e: xsbti.Exit   => assert(e.code == 0, "extension build failed, exitCode = " + e.code)
      case r: xsbti.Reboot => assert(true == false, "expected application to build, instead rebooted")
    }
    jar
  }

}
