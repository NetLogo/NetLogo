///
/// I18n
///

// path handling details are inelegant/repetitive, should be cleaned up - ST 5/30/12

resourceGenerators in Compile += Def.task {
  val names: Set[String] =
    IO.listFiles(file(".") / "dist" / "i18n")
      .map(_.getName)
      .filter(_.endsWith(".txt"))
      .map(_.stripSuffix(".txt"))
      .toSet
  val s = streams.value
  val cache =
    FileFunction.cached(s.cacheDirectory / "native2ascii",
        inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
      in: Set[File] =>
        names.map{name =>
          s.log.info(s"native2ascii: $name")
          native2ascii(resourceManaged.value, name)
        }
    }
  cache(names.map(name => file(".") / "dist" / "i18n" / (name + ".txt"))).toSeq
}.taskValue

def native2ascii(dir: File, name: String): File = {
  val in = file(".") / "dist" / "i18n" / (name + ".txt")
  val result = dir / (name + ".properties")
  IO.createDirectory(dir)
  (new sun.tools.native2ascii.Main).convert(
    Array("-encoding", "UTF-8", in.getPath, result.getPath))
  result
}

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
        Set(flex(s.log.info(_), base, src, "ImportLexer"),
            flex(s.log.info(_), base, src, "TokenLexer"))
    }
  cache(Set(base / "project" / "flex" / "warning.txt",
            base / "project" / "flex" / "ImportLexer.flex",
            base / "project" / "flex" / "TokenLexer.flex")).toSeq
}.taskValue

def flex(log: String => Unit, base: File, dir: File, kind: String): File = {
  val project = base / "project" / "flex"
  val result = dir / (kind + ".java")
  log("generating " + result)
  JFlex.Main.main(Array("--quiet", (project / (kind + ".flex")).asFile.toString))
  IO.write(result,
    IO.read(project / "warning.txt") +
    IO.read(project / (kind + ".java")))
  (project / (kind + ".java")).asFile.delete()
  result
}
