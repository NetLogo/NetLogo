import java.io.File
import sbt._
import Keys._

import Def.Initialize
import sbt.complete.{ Parser, DefaultParsers }, Parser.success, DefaultParsers._

object Extensions {

  private val extensionDeps = TaskKey[(File, File)]("extension dependencies")
  val extensionRoot = SettingKey[File]("extension root", "root directory of extensions")
  val excludedExtensions = SettingKey[Seq[String]]("extensions excluded for this configuration")
  val extensions = TaskKey[Seq[File]]("extensions", "builds extensions")
  val extension = InputKey[Seq[File]]("extension", "build a single extension")
  val forExtension = InputKey[Unit]("forExtension", "run an sbt command in a single extension")
  val forAllExtensions = InputKey[Unit]("forAllExtensions", "run an sbt command in all extensions")

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

  val sbtCommandParser: Parser[String] = {
    import Parser._
    (Space ~> StringBasic)
  }

  val initSbtCommandParser: Initialize[Parser[String]] =
    Def.setting(sbtCommandParser)

  val extensionAndCommandParser: Initialize[Parser[(File, String)]] = {
    import Parser._
    Def.bind(extensionParser)(e => Def.setting { e ~ sbtCommandParser })
  }

  lazy val settings = Seq(
    extensionDeps := {
      val packagedNetLogoJar     = (packageBin in Compile).value
      val packagedNetLogoTestJar = (packageBin in Test).value
      (packagedNetLogoJar, packagedNetLogoTestJar)
    },
    forExtension := {
      val (extensionDir, command) = extensionAndCommandParser.parsed
      val (packagedNetLogoJar, packagedNetLogoTestJar) = extensionDeps.value

      runExtensionCommand(extensionDir, state.value, packagedNetLogoJar, command)
    },
    forAllExtensions := {
      val command = initSbtCommandParser.parsed
      val (packagedNetLogoJar, packagedNetLogoTestJar) = extensionDeps.value
      val dirs = extensionDirs(extensionRoot.value)

      dirs.foreach { dir =>
        runExtensionCommand(dir, state.value, packagedNetLogoJar, command)
      }
    },
    extension  := {
      val extensionDir = extensionParser.parsed
      val (packagedNetLogoJar, packagedNetLogoTestJar) = extensionDeps.value
      streams.value.log.info("building extension: " + extensionDir.getName)
      buildExtension(extensionDir, packagedNetLogoJar, state.value)(Set()).toSeq
    },
    extensions := {
      val base                   = baseDirectory.value
      val (packagedNetLogoJar, packagedNetLogoTestJar) = extensionDeps.value
      val s = streams.value
      ("git -C " + base + " submodule --quiet update --init") ! s.log
      val dirs = extensionDirs(extensionRoot.value)
      dirs.filterNot(f => excludedExtensions.value.contains(f.getName)).flatMap{ dir =>
        cacheBuild(s.cacheDirectory, dir, Set(base / "NetLogo.jar", base / "NetLogoLite.jar")) {
          s.log.info("building extension: " + dir.getName)
          buildExtension(dir, packagedNetLogoJar, state.value) }
      }
    },
    excludedExtensions := Seq(),
    javaOptions +=
      "-Dnetlogo.extensions.dir=" + extensionRoot.value.getAbsolutePath.toString
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

  private def buildExtension(dir: File, netLogoJar: File, state: State): Set[File] => Set[File] = {
    runExtensionCommand(dir, state, netLogoJar, "package")

    { files => Set(dir / (dir.getName + ".jar")) }
  }

  private def runExtensionCommand(dir: File, state: State, netLogoJar: File, command: String): Unit = {
    System.setProperty("netlogo.jar.file", netLogoJar.getAbsolutePath)
    val buildConfig  = config(state, dir, command)
    runner.run(buildConfig) match {
      case e: xsbti.Exit   => assert(e.code == 0, "failed to build " + dir.getName + ", exitCode = " + e.code)
      case r: xsbti.Reboot => assert(true == false, "expected application to build, instead rebooted")
    }
  }

}
