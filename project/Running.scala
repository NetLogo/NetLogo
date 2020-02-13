import sbt._
import Keys._

import scala.util.Try

object Running {

  // the NetLogo app cannot be cleanly shut down, so we need a fresh JVM
  val settings = Seq(
    fork in run := true,
    javaOptions in run ++= Seq(
      "-XX:-OmitStackTraceInFastThrow",  // issue #104
      "-Xmx1024m",
      "-Dfile.encoding=UTF-8",
      "-Dapple.awt.graphics.UseQuartz=true") ++
    (if(System.getProperty("os.name").startsWith("Mac"))
      Seq("-Xdock:name=NetLogo")
      else Seq()) ++
    (if(System.getProperty("org.nlogo.is3d") == "true")
      Seq("-Dorg.nlogo.is3d=true")
     else Seq()) ++
    (if(System.getProperty("org.nlogo.noGenerator") == "true")
      Seq("-Dorg.nlogo.noGenerator=true")
     else Seq()) ++
    (if(System.getProperty("org.nlogo.noOptimizer") == "true")
      Seq("-Dorg.nlogo.noOptimizer=true")
     else Seq())
    )

  // In sbt 1.0, runMain started breaking the build. This is it's functional replacement, with a few more options thrown in
  def makeMainTask(
    mainClass: String,
    prefixArgs: Seq[String] = Seq.empty[String],
    classpath: Def.Initialize[Task[Seq[Attributed[File]]]] = (fullClasspath in Compile).toTask,
    workingDirectory: Def.Initialize[File] = baseDirectory,
    options: String = ""): Def.Initialize[InputTask[Try[Unit]]] = {
      import Def.spaceDelimited
      Def.inputTask {
        val args = spaceDelimited("").parsed
        val runner = new ForkRun(ForkOptions()
          .withWorkingDirectory(workingDirectory.value)
          .withRunJVMOptions(Vector("-Dorg.nlogo.is3d=" + System.getProperty("org.nlogo.is3d"))))
        runner.run(mainClass,
          classpath.value.map(_.data), prefixArgs ++ args, streams.value.log)
      }
  }
}
