import sbt._
import com.github.mustachejava._
import scala.collection.JavaConverters._

object Mustache {
  def apply(sourceFile: File, destFile: File, variables: Map[String, Object], rootFile: Option[File] = None): Unit = {
    val mf = rootFile.map(root => new DefaultMustacheFactory(root)).getOrElse(new DefaultMustacheFactory())

    val mustache = IO.reader(sourceFile)(mf.compile(_, sourceFile.getName))

    Using.fileWriter()(destFile) { wrtr => mustache.execute(wrtr, variables.asJava) }
  }

  def betweenDirectories(source: File, target: File, variables: Map[String, Object]): Seq[File] = {
    val pathMapping = Path.allSubpaths(source).map {
      case (f, p) => (f, target / p.stripSuffix(".mustache"))
    }

    val (templatedFiles, copiedFiles) = pathMapping.partition(_._1.getName.endsWith(".mustache"))

    FileActions.copyAll(copiedFiles)

    templatedFiles.foreach { case (src, dest) => Mustache(src, dest, variables, Some(source)) }

    (templatedFiles.map(_._2) ++ copiedFiles.map(_._2)).toSeq
  }
}
