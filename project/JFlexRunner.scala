import java.io.File
import sbt._
import Keys._

object JFlexRunner {

  val task =
    (cacheDirectory, sourceManaged in Compile, baseDirectory, streams) map {
      (cacheDir, src, base, s) =>
        val cache =
          FileFunction.cached(cacheDir / "lexers", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              Set(flex(s.log.info(_), base, src, "ImportLexer"),
                  flex(s.log.info(_), base, src, "TokenLexer"))
          }
        cache(Set(base / "project" / "warning.txt",
                  base / "project" / "ImportLexer.flex",
                  base / "project" / "TokenLexer.flex")).toSeq
    }

  // this used to be broken into two tasks, but jflex doesnt seem to be threadsafe
  // so we have to run them serially, which means we have to generate them both each time. -JC 6/8/10
  def flex(log: String => Unit, base: File, dir: File, kind: String): File = {
    val project = base / "project"
    val result = dir / (kind + ".java")
    log("generating " + result)
    JFlex.Main.main(Array("--quiet", (project / (kind + ".flex")).asFile.toString))
    IO.write(result,
      IO.read(project / "warning.txt") +
      IO.read(project / (kind + ".java")))
    (project / (kind + ".java")).asFile.delete()
    result
  }

}
