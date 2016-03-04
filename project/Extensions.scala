import java.io.File
import sbt._
import Keys._

import scala.collection.immutable.Set

object Extensions {

  val extensions = TaskKey[Seq[File]](
    "extensions", "builds extensions")

  val submoduleUpdate = TaskKey[Unit]("checks out all submodules")


  lazy val extensionsTask = Seq(
    submoduleUpdate := { "git submodule --quiet update --init" ! streams.value.log },
    extensions <<= Def.task {
      extensionDirs.value.map(ext =>
          cacheBuild(streams.value.cacheDirectory, fileDependencies.value, ext)(in =>
              buildExtension(ext, scalaInstance.value.libraryJar, streams.value.log)))
    }.dependsOn(submoduleUpdate, packageBin in Compile)
  )

  lazy val extensionDirs: Def.Initialize[Task[Seq[File]]] =
    Def.task {
      val validExtensionDirectory = new java.io.FileFilter {
        override def accept(f: File) =
          if (f.getName == "qtj")
            System.getProperty("os.name").startsWith("Mac")
          else
            f.isDirectory
      }
      IO.listFiles(validExtensionDirectory)(baseDirectory.value / "extensions").toSeq
    }

  lazy val fileDependencies: Def.Initialize[Set[File]] =
    Def.setting {
      Set(
        baseDirectory.value / "NetLogo.jar",
        baseDirectory.value / "NetLogoLite.jar"
      )
    }

  private def cacheBuild(cacheDirectory: File, deps: Set[File], dir: File)(build: File => File): File = {
    // we only generate one file, so we only return one file
    FileFunction.cached(cacheDirectory / "extensions" / dir.getName,
      inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) { files =>
        Set(build(dir))
      }(deps).head
  }

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
