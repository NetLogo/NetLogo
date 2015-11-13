import sbt._
import com.github.mustachejava._
import scala.collection.JavaConverters._

object Mustache {
  def apply(sourceFile: File, destFile: File, variables: Map[String, Object]): Unit = {
    val mf = new DefaultMustacheFactory()

    val mustache = IO.reader(sourceFile)(mf.compile(_, sourceFile.getName))

    Using.fileWriter()(destFile) { wrtr =>
      println("rendering: " + sourceFile.getName)
      mustache.execute(wrtr, variables.asJava)
    }
  }
}
