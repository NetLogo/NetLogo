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
    extensions <<= Def.taskDyn {
      new Scoped.RichTaskSeq(extensionDirs.value.map(buildExtensionTask)).join dependsOn(submoduleUpdate, packageBin in Compile)
    }
  )

  lazy val extensionDirs: Def.Initialize[Task[Seq[File]]] =
    Def.task {
      val isDirectory = new java.io.FileFilter {
        override def accept(f: File) = f.isDirectory
      }
      IO.listFiles(isDirectory)(baseDirectory.value / "extensions").toSeq
    }

  lazy val fileDependencies: Def.Initialize[Set[File]] =
    Def.setting {
      Set(
        baseDirectory.value / "NetLogo.jar",
        baseDirectory.value / "NetLogoLite.jar"
      )
    }

  private def buildExtensionTask(dir: File): Def.Initialize[Task[File]] =
    Def.task {
      // we only generate one file, so we only return one file
      FileFunction.cached(streams.value.cacheDirectory / "extensions" / dir.getName,
        inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
          in =>
            Set(buildExtension(dir, scalaInstance.value.libraryJar, streams.value.log))
        }(fileDependencies.value).head
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
