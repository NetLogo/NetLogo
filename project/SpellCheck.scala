// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

import java.io.File

import sbt.TaskKey

import scala.sys.process.Process

object SpellCheck {
  private val spellCheck = TaskKey[Unit]("spellCheck", "Spell check the documentation")

  lazy val settings = {
    spellCheck := {
      def recurseFiles(path: File): Seq[File] = {
        if (path.isDirectory) {
          path.listFiles.flatMap(recurseFiles)
        } else {
          val name = path.getName

          if (name.endsWith(".html.mustache") || name.endsWith(".md.mustache")) {
            Seq(path)
          } else {
            Seq()
          }
        }
      }

      recurseFiles(new File("autogen/docs")).foreach { path =>
        if (!path.toString.contains("behaviorspace-spanish")) {
          val lines: Seq[String] = (Process(path) #> "aspell -H -p ./dist/docwords.txt -d en_US list").lineStream

          if (lines.nonEmpty)
            println(lines.mkString(path.toString + "\n  ", "\n  ", ""))
        }
      }
    }
  }
}
