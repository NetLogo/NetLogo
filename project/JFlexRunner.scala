import java.io.File
import sbt._
import Keys._

object JFlexRunner {

  val task =
    Def.task {
      Seq(("agent", "ImportLexer"), ("lex", "TokenLexer")).map {
        case (pkg, kind) =>
          flex(streams.value.log.info(_), baseDirectory.value, streams.value.cacheDirectory, (sourceManaged in Compile).value, pkg, kind)
      }
    }

  // this used to be broken into two tasks, but jflex doesnt seem to be threadsafe
  // so we have to run them serially, which means we have to generate both files each time. -JC 6/8/10
  def flex(log: String => Unit, base: File, cacheDirectory: File, srcRoot: File, ppackage: String, kind: String): File = {
    val autogenFolder = base / "project" / "autogen"
    JFlex.Main.main(Array("--quiet", "-d", cacheDirectory.getPath, (autogenFolder / (kind + ".flex")).asFile.toString))
    val destination = srcRoot / "org" / "nlogo" / ppackage / (kind + ".java")
    log("creating " + srcRoot + "/org/nlogo/" + ppackage + "/" + kind + ".java")
    IO.write(destination,
      IO.read(autogenFolder / "warning.txt") +
      IO.read(cacheDirectory / (kind + ".java")))
    IO.delete(cacheDirectory / (kind + ".java"))
    destination
  }

}
