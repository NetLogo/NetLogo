import java.io.File
import sbt._
import Keys._

object Extensions {

  val extensions = TaskKey[Seq[File]](
    "extensions", "builds extensions")

  val extensionsTask =
    extensions <<= (baseDirectory, cacheDirectory, scalaInstance, streams) map {
      (base, cacheDir, scala, s) =>
        "git submodule --quiet update --init" ! s.log
        val isDirectory = new java.io.FileFilter {
          override def accept(f: File) = f.isDirectory
        }
        val dirs = IO.listFiles(isDirectory)(base / "extensions").toSet
        val cache =
          FileFunction.cached(cacheDir / "extensions", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in =>
              dirs.map(buildExtension(_, scala.libraryJar, s.log))
          }
        cache(Set(base / "NetLogo.jar",
                  base / "NetLogoLite.jar")).toSeq
    } dependsOn(packageBin in Compile)

  private def buildExtension(dir: File, scalaLibrary: File, log: Logger): File = {
    log.info("building extension: " + dir.getName)
    val jar = dir / (dir.getName + ".jar")
    val exitCode =
      if((dir / "build.sbt").exists)
        Process(Seq("bin/sbt", "package"), dir,
                "SCALA_JAR" -> scalaLibrary.getPath) ! log
      else
        Process(Seq("make", "-s", jar.getName), dir,
                "SCALA_JAR" -> scalaLibrary.getPath) ! log
    assert(exitCode == 0, "extension build failed, exitCode = " + exitCode)
    jar
  }

}
