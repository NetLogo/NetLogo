import java.io.File
import sbt._
import Keys._

object Autogen {

  val sourceGeneratorTask =
    (cacheDirectory, scalaSource in Compile, javaSource in Compile, baseDirectory, streams) map {
      (cacheDir, sdir, jdir, base, s) =>
        val cache =
          FileFunction.cached(cacheDir / "autogen", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              Set(flex(s.log.info(_), base, jdir, "agent", "ImportLexer"),
                  flex(s.log.info(_), base, jdir, "lex", "TokenLexer"))
          }
        cache(Set(base / "project" / "autogen" / "warning.txt",
                  base / "project" / "autogen" / "ImportLexer.flex",
                  base / "project" / "autogen" / "TokenLexer.flex")).toSeq
    }

  // this used to be broken into two tasks, but jflex doesnt seem to be threadsafe
  // so we have to run them serially, which means we have to generate them both each time. -JC 6/8/10
  def flex(log: String => Unit, base: File, dir: File, ppackage: String, kind: String): File = {
    val project = base / "project"
    log("generating " + kind + ".java")
    JFlex.Main.main(Array("--quiet", (project / (kind + ".flex")).asFile.toString))
    log("creating src/main/org/nlogo/" + ppackage + "/" + kind + ".java")
    val nlogoPackage = dir / "org" / "nlogo"
    val result = nlogoPackage / ppackage / (kind + ".java")
    IO.write(result,
      IO.read(project / "warning.txt") +
      IO.read(project / (kind + ".java")))
    (project / (kind + ".java")).asFile.delete()
    result
  }

}
