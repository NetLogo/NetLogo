// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

import java.io.File

import sbt.TaskKey

import scala.io.Source

object Ruler {
  private val ruler = TaskKey[Unit]("ruler", "Count lines of Java and Scala code in repo")

  lazy val settings = {
    ruler := {
      val format = "%25s: %6d unique, %6d total  (%3d%% Scala)\n"
      var tj, tuj, ts, tus = 0 // totals. u = unique, j = Java, s = Scala

      def percent(n: Int, d: Int): Int =
        (100 * n / d).floor.toInt

      // for extensions we don't want to break it out into packages, we just
      // want to "flatten" all the packages into one entry
      def names(dir: File, extension: String, flatten: Boolean): Seq[String] = {
        dir.listFiles.flatMap { path =>
          if (path.isDirectory) {
            if (flatten) {
              names(path, extension, flatten)
            } else {
              Seq()
            }
          } else {
            val name = path.getName

            if (name.endsWith(extension)) {
              Seq(path.toString.replace("\\", "/"))
            } else {
              Seq()
            }
          }
        }
      }

      def lines(dir: File, extension: String, flatten: Boolean): Seq[String] = {
        names(dir, extension, flatten).flatMap { name =>
          val source = Source.fromFile(name)
          val lines = source.getLines.toList

          source.close()

          lines
        }
      }

      def dirs(root: File): Seq[File] = {
        root.listFiles.flatMap { path =>
          if (path.isDirectory) {
            val name = path.toString.replace("\\", "/")

            if (name.containsSlice("/.git/") || name.containsSlice("/build/") ||
                name.matches("extensions/.*/src/.*/.*")) {
              dirs(path)
            } else {
              path +: dirs(path)
            }
          } else {
            Seq()
          }
        }
      }

      def outputLines(root: String) = {
        for { dir <- dirs(new File(root))
              dirString = dir.toString.replace("\\", "/")
              flatten = dirString.matches("extensions/.*/src/.*")
              j = lines(dir, ".java", flatten)
              uj = j.distinct
              s = lines(dir, ".scala", flatten)
              us = s.distinct
              if j.nonEmpty || s.nonEmpty
        } yield {
          tj += j.size
          tuj += uj.size
          ts += s.size
          tus += us.size

          format.format(dirString.replaceAll(root + "/org/nlogo/", "")
                                  .replaceAll(".src.org", "")
                                  .replaceAll(root + "/", "")
                                  .replaceFirst(".src$", "")
                                  .replaceAll("/", "."),
                        uj.size + us.size, j.size + s.size,
                        percent(us.size, uj.size + us.size))
        }
      }

      def firstNumber(s: String): Int =
        s.dropWhile(!_.isDigit).takeWhile(_.isDigit).mkString.toInt

      def sortAndPrint(root: String): Unit = {
        println(s"$root:")
        outputLines(root).toList.sortBy(firstNumber).reverse.foreach(print)
        println()
      }

      sortAndPrint("netlogo-gui/src/main")
      sortAndPrint("netlogo-gui/src/test")
      sortAndPrint("netlogo-gui/src/tools")
      sortAndPrint("netlogo-core/src/main")
      sortAndPrint("netlogo-core/src/test")
      sortAndPrint("netlogo-headless/src/main")
      sortAndPrint("netlogo-headless/src/test")
      sortAndPrint("parser-core")
      sortAndPrint("parser-js")
      sortAndPrint("parser-jvm")
      sortAndPrint("extensions")
      sortAndPrint("project")
      sortAndPrint("bin")

      println(format.format("TOTAL", tuj + tus, tj + ts, percent(tus, tuj + tus)))
    }
  }
}
