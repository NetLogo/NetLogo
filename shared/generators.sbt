///
/// I18n
///

// path handling details are inelegant/repetitive, should be cleaned up - ST 5/30/12

resourceGenerators in Compile += Def.task {
  val names: Set[String] =
    IO.listFiles(baseDirectory.value / "i18n")
      .map(_.getName)
      .filter(_.endsWith(".txt"))
      .map(_.stripSuffix(".txt"))
      .toSet
  val s = streams.value
  val cache =
    FileFunction.cached(s.cacheDirectory / "native2ascii",
        inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
      in: Set[File] =>
        names.map{ name =>
          s.log.info(s"native2ascii: $name")
          native2ascii(resourceManaged.value, baseDirectory.value / "i18n", name)
        }
    }
  cache(names.map(name => baseDirectory.value / "i18n" / (name + ".txt"))).toSeq
}.taskValue

def native2ascii(dir: File, i18nDir: File, name: String): File = {
  val in = i18nDir / (name + ".txt")
  val result = dir / (name + ".properties")
  IO.createDirectory(dir)
  (new sun.tools.native2ascii.Main).convert(
    Array("-encoding", "UTF-8", in.getPath, result.getPath))
  result
}

