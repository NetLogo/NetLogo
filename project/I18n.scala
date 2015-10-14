import java.io.File
import sbt._
import Keys._

object I18n {

  // path handling details are inelegant/repetitive, should be cleaned up - ST 5/30/12

  val resourceGeneratorTask =
    Def.task {
      val i18nDir = baseDirectory.value / "project" / "autogen" / "i18n"
      val names: Set[String] =
        IO.listFiles(i18nDir).map(_.getName).filter(_.endsWith(".txt")).map(_.dropRight(4)).toSet
      val cache =
        FileFunction.cached(streams.value.cacheDirectory / "native2ascii", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
          in: Set[File] =>
            names.map(name => native2ascii(streams.value.log.info(_), i18nDir, resourceManaged.value, name))
        }
      cache(names.map(name => i18nDir / (name + ".txt"))).toSeq
    }

  def native2ascii(log: String => Unit, i18nDir: File, dir: File, name: String): File = {
    log("native2ascii: " + name)
    val in = i18nDir / (name + ".txt")
    val result = dir / (name + ".properties")
    IO.createDirectory(dir)
    (new sun.tools.native2ascii.Main).convert(
      Array("-encoding", "UTF-8", in.getPath, result.getPath))
    result
  }

}
