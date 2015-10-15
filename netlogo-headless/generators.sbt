///
/// JFlex
///

sourceGenerators in Compile += Def.task[Seq[File]] {
  val src = (sourceManaged in Compile).value
  val base = baseDirectory.value
  val s = streams.value
  val cache =
    FileFunction.cached(s.cacheDirectory / "lexers", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
      in: Set[File] =>
        Set(flex(s.log.info(_), base, s.cacheDirectory, src, "agent", "ImportLexer"))
    }
  cache(Set(base / "project" / "flex" / "warning.txt",
            base / "project" / "flex" / "ImportLexer.flex")).toSeq
}.taskValue

def flex(log: String => Unit, base: File, cacheDirectory: File, dir: File, ppackage: String, kind: String): File = {
  val project = base / "project" / "flex"
  val result = dir / "org" / "nlogo" / ppackage / (kind + ".java")
  log("generating " + result)
  JFlex.Main.main(Array("--quiet", "-d", cacheDirectory.getPath, (project / (kind + ".flex")).asFile.toString))
  IO.write(result,
    IO.read(project / "warning.txt") +
    IO.read(cacheDirectory / (kind + ".java")))
  IO.delete(cacheDirectory / (kind + ".java"))
  result
}
