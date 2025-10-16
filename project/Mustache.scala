import sbt._, io.Using

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.reflect.{ MissingWrapper, ReflectionObjectHandler }
import com.github.mustachejava.util.Wrapper

import java.util.{ List => JList }

object Mustache {
  def apply(sourceFile: File, destFile: File, variables: Map[String, AnyRef], rootFile: Option[File] = None): Unit = {
    import scala.collection.JavaConverters._

    val mf = rootFile.map(new DefaultMustacheFactory(_)).getOrElse(new DefaultMustacheFactory)

    // by default, Mustache silently ignores missing template variables, which makes it very easy to accidentally
    // leave out a variable that was supposed to be included, resulting in potentially incorrect output files that
    // may go undetected. this code catches such cases and explicitly throws an exception instead of ignoring it.
    // (Isaac B 10/16/25)
    mf.setObjectHandler(new ReflectionObjectHandler {
      override def find(name: String, scopes: JList[AnyRef]): Wrapper = {
        super.find(name, scopes) match {
          case _: MissingWrapper =>
            throw new Exception

          case w => w
        }
      }
    })

    val mustache = IO.reader(sourceFile)(mf.compile(_, sourceFile.getName))
    Using.fileWriter()(destFile) { wrtr => mustache.execute(wrtr, variables.asJava) }
  }

  def betweenDirectories(source: File, target: File, titleMapping: Map[String, String],
    variables: Map[String, AnyRef], excludedFiles: Set[String] = Set()): Seq[File] = {

    val pathMapping = Path.allSubpaths(source).map {
      case (f, p) => (f, target / p.stripSuffix(".mustache"))
    }
    val (templatedFiles, copiedFiles) = pathMapping.partition(_._1.getName.endsWith(".mustache"))
    FileActions.copyAll(copiedFiles)
    templatedFiles.foreach {
      case (src, dest) =>
        if (!excludedFiles.contains(src.getName)) {
          Mustache(src, dest, variables + ("title" -> titleMapping.getOrElse(dest.getName.stripSuffix(".md"), "")),
                   Some(source))
        }
    }
    (templatedFiles ++ copiedFiles).map(_._2).toSeq
  }
}
