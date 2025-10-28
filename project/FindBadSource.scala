// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

import java.io.File

import sbt.TaskKey

import scala.io.Source

object FindBadSource {
  private val findBadSource = TaskKey[Unit]("findBadSource", "Search text files in the repo for general formatting issues")

  lazy val settings = {
    findBadSource := {
      // probably there are a lot more that could be here
      val extensions =
        List("java", "scala", "py", "txt", "sh", "nlogo", "nlogo3d", "nlogox", "nlogox3d", "html", "css", "properties",
             "md", "csv", "asc", "prj", "xml")

      def ignore(path: String): Boolean = {
        path.contains("/extensions/gis/") ||
        path.contains("/src_managed/") ||
        path.contains("/tmp/") ||
        path.endsWith("Lexer.java") ||
        path.endsWith("net-logo-web.html") ||
        path.startsWith("./.idea/") ||
        path.startsWith("./docs/scaladoc/") ||
        path.startsWith("./dist/i18n/") ||
        !extensions.exists(path.endsWith)
      }

      val directories =
        List("extensions", "netlogo-gui", "netlogo-core", "netlogo-headless", "parser-core", "parser-js", "parser-jvm",
             "test")

      def recurseDirectories(dir: File): Unit = {
        dir.listFiles.foreach { path =>
          if (path.isDirectory) {
            recurseDirectories(path)
          } else if (!ignore(path.getAbsolutePath.replace("\\", "/"))) {
            val source = Source.fromFile(path)
            val contents = source.mkString

            source.close()

            var problems = Seq[String]()

            if (contents.nonEmpty && contents.last != '\n')
              problems = problems :+ "Missing newline at eof"

            if (contents.contains('\r'))
              problems = problems :+ "Carriage return character(s) found"

            if (contents.contains('\t'))
              problems = problems :+ "Tab character(s) found"

            if (problems.nonEmpty) {
              println(path)

              problems.foreach(p => println(s"  $p"))
            }
          }
        }
      }

      directories.foreach(dir => recurseDirectories(new File(dir)))
    }
  }
}
