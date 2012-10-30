import java.io.File
import sbt._
import Keys._

object Extensions {

  val extensions = TaskKey[Seq[File]](
    "extensions", "builds extensions")

  val extensionsTask =
    extensions <<= (baseDirectory, scalaInstance, streams) map {
      (base, scala, s) =>
        "git submodule --quiet update --init" ! s.log
        val isDirectory = new java.io.FileFilter {
          override def accept(f: File) = f.isDirectory
        }
        // temporarily disabling gogo extension build until I figure out
        // what in it is crashing the Scala 2.10.0-RC1 compiler - ST 10/30/12
        for(dir <- IO.listFiles(isDirectory)(base / "extensions").toSeq if dir.getName != "gogo")
        yield buildExtension(dir, scala.libraryJar, s.log)
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
