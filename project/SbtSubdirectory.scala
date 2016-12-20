import java.io.File
import sbt._
import Keys.state
import Def.Initialize

object SbtSubdirectory {

  class NestedConfiguration(val config: xsbti.AppConfiguration, baseDir: File, args: Array[String]) extends xsbti.AppConfiguration {
    override val arguments = args
    override val provider = config.provider
    override val baseDirectory = baseDir
  }

  val runner = new xMain()

  def subdirectoryCommandTask(dir: File, nlJar: Initialize[Task[File]], commands: Seq[String]): Initialize[Task[Unit]] =
    Def.task {
      runSubdirectoryCommand(dir, state.value, nlJar.value, commands)
    }

  def runSubdirectoryCommand(dir: File, state: State, netLogoJar: File, commands: Seq[String]): Unit = {
    System.setProperty("netlogo.jar.file", netLogoJar.getAbsolutePath)
    val buildConfig  = new NestedConfiguration(state.configuration, dir, commands.toArray)
    runner.run(buildConfig) match {
      case e: xsbti.Exit   => assert(e.code == 0, "failed to build " + dir.getName + ", exitCode = " + e.code)
      case r: xsbti.Reboot => assert(true == false, "expected application to build, instead rebooted")
    }
  }
}
