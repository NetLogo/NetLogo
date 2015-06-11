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
        val dirs = IO.listFiles(isDirectory)(base / "extensions").toSeq
        val caches = dirs.map{dir =>
          FileFunction.cached(s.cacheDirectory / "extensions" / dir.getName,
                              inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in =>
              Set(buildExtension(dir, scala.libraryJar, s.log))
          }}
        caches.flatMap{cache => cache(Set(base / "NetLogo.jar", base / "NetLogoLite.jar"))}
    } dependsOn(packageBin in Compile)

  private def buildExtension(dir: File, scalaLibrary: File, log: Logger): File = {
    log.info("building extension: " + dir.getName)
    val jar = dir / (dir.getName + ".jar")
    val exitCode =
      if((dir / "build.sbt").exists && (dir / "bin" / "sbt").exists)
        Process(Seq("bin/sbt", "package"), dir,
                "SCALA_JAR" -> scalaLibrary.getPath) ! log
      else if((dir / "build.sbt").exists)
        Process(Seq("./sbt", "package"), dir,
                "SCALA_JAR" -> scalaLibrary.getPath) ! log
      else
        Process(Seq("make", "-s", jar.getName), dir,
                "SCALA_JAR" -> scalaLibrary.getPath) ! log
    assert(exitCode == 0, "extension build failed, exitCode = " + exitCode)
    jar
  }

}
