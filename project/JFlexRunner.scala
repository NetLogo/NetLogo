import java.io.File
import sbt._
import Keys._

import NetLogoBuild.autogenRoot

object JFlexRunner {

  lazy val settings = Seq(sourceGenerators in Compile += task.taskValue)

  lazy val task =
    Def.task {
      val cachedLexers = FileFunction.cached(streams.value.cacheDirectory / "lexer", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
        (in: Set[File]) =>
          Set(("agent", "ImportLexer")).map {
            case (pkg, kind) =>
              flex(streams.value.log.info(_), autogenRoot.value, streams.value.cacheDirectory, (sourceManaged in Compile).value, pkg, kind)
        }
      }
      cachedLexers(Set(
          autogenRoot.value / "flex" / "ImportLexer.flex",
          autogenRoot.value / "flex" / "warning.txt")).toSeq
    }

  // this used to be broken into two tasks, but jflex doesnt seem to be threadsafe
  // so we have to run them serially, which means we have to generate both files each time. -JC 6/8/10
  def flex(log: String => Unit, autogenFolder: File, cacheDirectory: File, srcRoot: File, ppackage: String, kind: String): File = {
    JFlex.Main.main(Array("--quiet", "-d", cacheDirectory.getPath, (autogenFolder / "flex" / (kind + ".flex")).asFile.toString))
    val destination = srcRoot / "org" / "nlogo" / ppackage / (kind + ".java")
    log("creating " + srcRoot + "/org/nlogo/" + ppackage + "/" + kind + ".java")
    IO.write(destination,
      IO.read(autogenFolder / "flex" / "warning.txt") +
      IO.read(cacheDirectory / (kind + ".java")))
    IO.delete(cacheDirectory / (kind + ".java"))
    destination
  }

}
