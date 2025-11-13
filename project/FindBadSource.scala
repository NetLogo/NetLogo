// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

import java.io.File
import java.nio.charset.MalformedInputException

import sbt.TaskKey

import scala.io.Source
import scala.sys.process.Process

object FindBadSource {
  private val findBadSource =
    TaskKey[Unit]("findBadSource", "Search text files in the repo for general formatting issues")

  lazy val settings = {
    findBadSource := {
      val rules = Seq("*.scala", "*.java", "*.html", "*.md", "*.mustache", "*.css", "*.js", ":!:*.min.js", "*.nlogox*")

      Process(Seq("git", "ls-files", "--recurse-submodules", "--") ++ rules).!!
        .split("\n").foreach { path =>

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

        if (contents.split("\n").exists("\\s+$".r.findFirstIn(_).isDefined))
          problems = problems :+ "Trailing whitespace found"

        if (problems.nonEmpty) {
          println(path)

          problems.foreach(p => println(s"  $p"))
        }
      }
    }
  }
}
