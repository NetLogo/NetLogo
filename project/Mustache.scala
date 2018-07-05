import sbt._, io.Using

import com.github.mustachejava._

object Mustache {
  def apply(sourceFile: File, destFile: File, variables: Map[String, AnyRef], rootFile: Option[File] = None): Unit = {
    import scala.collection.JavaConverters._

    val mf = rootFile.map(new DefaultMustacheFactory(_)).getOrElse(new DefaultMustacheFactory)
    val mustache = IO.reader(sourceFile)(mf.compile(_, sourceFile.getName))
    Using.fileWriter()(destFile) { wrtr => mustache.execute(wrtr, variables.asJava) }
  }

  def betweenDirectories(source: File, target: File, titleMapping: Map[String, String],
    variables: Map[String, AnyRef]): Seq[File] = {

    val pathMapping = Path.allSubpaths(source).map {
      case (f, p) => (f, target / p.stripSuffix(".mustache"))
    }
    val (templatedFiles, copiedFiles) = pathMapping.partition(_._1.getName.endsWith(".mustache"))
    FileActions.copyAll(copiedFiles)
    templatedFiles.foreach {
      case (src, dest) =>
        val titleVar = titleMapping.get(dest.getName.stripSuffix(".md")).map("title" -> _)
        Mustache(src, dest, variables ++ titleVar, Some(source))
    }
    (templatedFiles ++ copiedFiles).map(_._2).toSeq
  }
}
