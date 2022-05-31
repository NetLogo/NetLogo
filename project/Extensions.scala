import java.io.File
import sbt._
import Keys._

import Def.Initialize
import sbt.complete.{ Parser, DefaultParsers }, Parser.success, DefaultParsers._
import SbtSubdirectory.runSubdirectoryCommand

import scala.sys.process.Process

object Extensions {

  private val extensionNetLogoJar = TaskKey[File]("netlogo jar, ensuring test jar also built")
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
    extensionNetLogoJar := {
      (packageBin in Test).value
      (packageBin in Compile).value
    },
    forExtension := {
      val (extensionDir, command) = extensionAndCommandParser.parsed

      runSubdirectoryCommand(extensionDir, state.value, extensionNetLogoJar.value, Seq(command))
    },
    forAllExtensions := {
      val command = initSbtCommandParser.parsed
      val dirs = extensionDirs(extensionRoot.value)

      dirs.foreach { dir =>
        runSubdirectoryCommand(dir, state.value, extensionNetLogoJar.value, Seq(command))
      }
    },
    extension  := {
      val extensionDir = extensionParser.parsed
      streams.value.log.info("building extension: " + extensionDir.getName)
      buildExtension(extensionDir, extensionNetLogoJar.value, state.value)(Set()).toSeq
    },
    extensions := {
      val nlJar = extensionNetLogoJar.value
      val excluded = excludedExtensions.value
      val base = baseDirectory.value
      val s    = streams.value
      Process("git -C " + base + " submodule --quiet update --init") ! s.log
      val dirs = extensionDirs(extensionRoot.value)
      val stateValue = state.value
      dirs.filterNot(f => excluded.contains(f.getName)).flatMap{ dir =>
        cacheBuild(s.cacheDirectory, dir, Set(base / "NetLogo.jar", base / "NetLogoLite.jar")) {
          s.log.info("building extension: " + dir.getName)
          buildExtension(dir, nlJar, stateValue) }
      }
    },
    excludedExtensions := Seq("nw"),
    javaOptions +=
      "-Dnetlogo.extensions.dir=" + extensionRoot.value.getAbsolutePath.toString
  )

  private def cacheBuild(cacheDirectory: File, extensionDir: File, otherDeps: Set[File])
                        (build: Set[File] => Set[File]): Seq[File] = {
    val buildCached = FileFunction.cached(cacheDirectory / "extensions" / extensionDir.getName,
        inStyle = FilesInfo.hash, outStyle = FilesInfo.hash)(build)
    buildCached(otherDeps).toSeq
  }

  private def buildExtension(dir: File, netLogoJar: File, state: State): Set[File] => Set[File] = {
    runSubdirectoryCommand(dir, state, netLogoJar, Seq("package"))

    { files => Set(dir / (dir.getName + ".jar")) }
  }
}
