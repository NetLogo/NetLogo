import java.io.File
import sbt._
import Keys._

import Def.Initialize
import sbt.complete.{ Parser, DefaultParsers }, Parser.success, DefaultParsers._

object Extensions {

  private val extensionDeps = TaskKey[(File, File)]("extension dependencies")
  val extensionRoot = SettingKey[File]("extension root", "root directory of extensions")
  val extensions = TaskKey[Seq[File]]("extensions", "builds extensions")
  val extension = InputKey[Seq[File]]("extension", "build a single extension")

  val isDirectory = new java.io.FileFilter {
    override def accept(f: File) = f.isDirectory
  }

  def extensionDirs(base: File) = IO.listFiles(isDirectory)(base).toSeq

  val extensionParser: Initialize[Parser[File]] = {
    import Parser._
    Def.setting {
      (Space ~> extensionDirs(extensionRoot.value)
        .map(d => (d.getName ^^^ d)).reduce(_ | _))
    }
  }

  val extensionsTask = Seq(
    extensionDeps := {
      val packagedNetLogoJar     = (packageBin in Compile).value
      val packagedNetLogoTestJar = (packageBin in Test).value
      (packagedNetLogoJar, packagedNetLogoTestJar)
    },
    extension  := {
      val extensionDir = extensionParser.parsed
      val (packagedNetLogoJar, packagedNetLogoTestJar) = extensionDeps.value
      val s = streams.value
      val scala = scalaInstance.value

      buildExtension(extensionDir, scala.libraryJar, packagedNetLogoJar, s.log, state.value)(Set()).toSeq
    },
    extensions := {
      val base                   = baseDirectory.value
      val (packagedNetLogoJar, packagedNetLogoTestJar) = extensionDeps.value
      val s = streams.value
      val scala = scalaInstance.value
      ("git -C " + base + " submodule --quiet update --init") ! s.log
      val dirs = extensionDirs(extensionRoot.value)
      dirs.flatMap{ dir =>
        cacheBuild(s.cacheDirectory, dir, Set(base / "NetLogo.jar", base / "NetLogoLite.jar"))(
          buildExtension(dir, scala.libraryJar, packagedNetLogoJar, s.log, state.value))
      }
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

  private def cacheBuild(cacheDirectory: File, extensionDir: File, otherDeps: Set[File])
                        (build: Set[File] => Set[File]): Seq[File] = {
    val buildCached = FileFunction.cached(cacheDirectory / "extensions" / extensionDir.getName,
        inStyle = FilesInfo.hash, outStyle = FilesInfo.hash)(build)
    buildCached(otherDeps).toSeq
  }

  private def buildExtension(dir: File, scalaLibrary: File, netLogoJar: File, log: Logger, state: State): Set[File] => Set[File] = {
    log.info("building extension: " + dir.getName)
    System.setProperty("netlogo.jar.url", netLogoJar.toURI.toString)
    val buildConfig  = config(state, dir, "package")
    val jar = dir / (dir.getName + ".jar")
    runner.run(buildConfig) match {
      case e: xsbti.Exit   => assert(e.code == 0, "extension build failed, exitCode = " + e.code)
      case r: xsbti.Reboot => assert(true == false, "expected application to build, instead rebooted")
    }
    { files => Set(jar) }
  }

}
