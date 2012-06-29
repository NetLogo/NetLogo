import java.io.File
import sbt._
import Keys._

object Extensions {

  val extensions = TaskKey[Seq[File]](
    "extensions", "builds extensions")

  val extensionsTask =
    extensions <<= (baseDirectory, scalaInstance, streams) map { (base, scala, s) =>
      "git submodule --quiet update --init" ! s.log
      val isDirectory = new java.io.FileFilter {
        override def accept(f: File) = f.isDirectory
      }
      val dirs = IO.listFiles(isDirectory)(base / "extensions").toSeq
      dirs.map(buildExtension(_, scala.libraryJar, s.log))
    } dependsOn(packageBin in Compile)

  private def buildExtension(dir: File, scalaLibrary: File, log: Logger): File = {
    log.info("building extension: " + dir.getName)
    val jar = dir / (dir.getName + ".jar")
    if((dir / "build.sbt").exists)
      Process(Seq("bin/sbt", "package"), dir,
              "SCALA_JAR" -> scalaLibrary.getPath) ! log
    else
      Process(List("make", "-s", jar.getName), dir,
              "SCALA_JAR" -> scalaLibrary.getPath) ! log
    jar
  }

}
