import java.io.File
import sbt._
import Keys._

object Extensions {

  val extensions = taskKey[Seq[File]](
    "builds extensions")

  val extensionsTask =
    extensions := {
      "git submodule --quiet update --init" ! streams.value.log
      val isDirectory = new java.io.FileFilter {
        override def accept(f: File) = f.isDirectory
      }
      val dirs = IO.listFiles(isDirectory)(baseDirectory.value / "extensions")
      for(dir <- dirs.toSeq)
      yield buildExtension(dir, scalaInstance.value.libraryJar, streams.value.log)
    }

  private def buildExtension(dir: File, scalaLibrary: File, log: Logger): File = {
    log.info("extension: " + dir.getName)
    val jar = dir / (dir.getName + ".jar")
    val exitCode =
      if((dir / "build.sbt").exists)
        Process(Seq("./sbt", "package"), dir,
                "SCALA_JAR" -> scalaLibrary.getPath) ! log
      else
        Process(Seq("make", "-s", jar.getName), dir,
                "SCALA_JAR" -> scalaLibrary.getPath) ! log
    assert(exitCode == 0, "extension build failed, exitCode = " + exitCode)
    jar
  }

}
