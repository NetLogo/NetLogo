// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

import java.io.{ File, FileWriter }

import scala.collection.mutable.LinkedHashMap

import sbt.{ Def, InputKey }

import scala.io.Source

object TranslationHelper
{
  private val translationHelper = InputKey[Unit]("translationHelper", "Helper for updating I18N files")

  lazy val settings = {
    translationHelper := {
      helper(Def.spaceDelimited("").parsed.iterator)
    }
  }

  private def helper(args: Iterator[String]): Unit = {
    var mode = ""
    var current = ""
    var currentTranslated = ""
    var previous = ""
    var previousTranslated = ""
    var output = ""
    var reference: Option[String] = None
    var filter = ""
    var startRange = 0
    var rangeLength = -1

    while (args.hasNext) {
      val arg = args.next().trim

      arg match {
        case "-h" => return printHelp()
        case "-g" | "-m" => mode = arg
        case "-c" => current = args.next().trim
        case "-n" => currentTranslated = args.next().trim
        case "-d" => previous = args.next().trim
        case "-t" => previousTranslated = args.next().trim
        case "-o" => output = args.next().trim
        case "-r" => reference = Option(args.next().trim)
        case "-f" => filter = args.next().trim
        case "-s" => startRange = args.next().trim.toInt
        case "-l" => rangeLength = args.next().trim.toInt
        case _ => return println(s"Error: invalid flag '$arg'.")
      }
    }

    if (mode == "-g") {
      if (current.isEmpty || output.isEmpty)
        return println("Error: incorrect arguments provided for mode 'generate'.")

      val properties: LinkedHashMap[String, String] = {
        if (previous.isEmpty) {
          LinkedHashMap[String, String]()
        } else {
          selectLines(previous, filter)
        }
      }

      val propertiesTranslated: LinkedHashMap[String, String] = {
        if (previousTranslated.isEmpty) {
          LinkedHashMap[String, String]()
        } else {
          selectLines(previousTranslated, filter)
        }
      }

      val writer = new FileWriter(new File(output))
      val refWriter: Option[FileWriter] = reference.map(path => new FileWriter(new File(path)))

      var currentLines = selectLines(current, filter).drop(startRange)

      if (rangeLength != -1)
        currentLines = currentLines.take(rangeLength)

      for ((p, v) <- currentLines) {
        if (!propertiesTranslated.contains(p) || (properties.contains(p) && properties(p) != v)) {
          writer.write(s"Property: ${p}\n")

          if (propertiesTranslated.contains(p)) {
            writer.write(s"Previous English: ${addBreaks(properties(p))}\n")
            writer.write(s"Previous Translation: ${addBreaks(propertiesTranslated(p))}\n")
          }

          writer.write(s"English: ${addBreaks(v)}\n")
          writer.write("Translation: \n\n")
        } else if (propertiesTranslated.contains(p)) {
          refWriter.foreach { writer =>
            writer.write(s"Property: ${p}\n")
            writer.write(s"English: ${addBreaks(v)}\n")
            writer.write(s"Translation: ${addBreaks(propertiesTranslated(p))}\n\n")
          }
        }
      }

      writer.close()
      refWriter.foreach(_.close())
    } else if (mode == "-m") {
      if (current.isEmpty || output.isEmpty)
        return println("Error: incorrect arguments provided for mode 'merge'.")

      val properties: LinkedHashMap[String, String] = {
        if (previousTranslated.isEmpty) {
          LinkedHashMap[String, String]()
        } else {
          getOrderedProperties(previousTranslated)
        }
      }

      val newProperties: Iterator[String] = {
        if (currentTranslated.isEmpty) {
          Iterator[String]()
        } else {
          val source = Source.fromFile(currentTranslated)
          val lines = source.getLines

          source.close()

          lines
        }
      }

      while (newProperties.hasNext) {
        val next = getNextLine(newProperties)

        if (next.startsWith("Property:")) {
          val name = next.replace("Property:", "").trim

          if (name.nonEmpty) {
            if (getNextLine(newProperties).startsWith("Previous English:")) {
              getNextLine(newProperties)
              getNextLine(newProperties)
            }

            val translated = getNextLine(newProperties).replace("Translation:", "").trim

            if (translated.isEmpty) {
              properties.remove(name)
            } else {
              properties(name) = translated
            }
          }
        } else {
          val parts = next.split(" = ", 2)

          if (parts.length >= 2) {
            if (parts(1).trim.isEmpty) {
              properties.remove(parts(0).trim)
            } else {
              properties(parts(0).trim) = parts(1).trim
            }
          }
        }
      }

      val writer = new FileWriter(new File(output))

      for ((p, v) <- getOrderedProperties(current)) {
        if (properties.contains(p)) {
          if (p.startsWith("#")) {
            writer.write(p + "\n")
          } else {
            writer.write(s"$p = ${addBreaks(properties(p), false)}\n")
          }
        }
      }

      writer.close()
    } else {
      println("Error: no mode specified.")
    }
  }

  private def printHelp(): Unit = {
    println("""general
              |
              |-h   display help information
              |
              |mode specification
              |
              |-g   generate new translator file
              |-m   merge translator file with existing translations
              |
              |generate mode
              |
              |-c <path>   current english properties file
              |-d <path>   outdated english properties file
              |-t <path>   outdated translated properties file
              |-f <string>   property name filter (regex)
              |-o <path>   output file
              |-r <path>   reference output file (includes previously translated properties)
              |-s <int>   number of filtered properties to skip before starting output
              |-l <int>   number of properties to include in output
              |
              |merge mode
              |
              |-c <path>   current english properties file
              |-t <path>   outdated translated properties file
              |-n <path>   new translated properties file
              |-o <path>   output file""".stripMargin)
  }

  private def getOrderedProperties(path: String): LinkedHashMap[String, String] = {
    val r = "(([\\w.]+)\\s*[=:]\\s*([^\n]+))|(#[^\n]+)".r

    val source = Source.fromFile(path)

    val items = r.findAllMatchIn(source.getLines.mkString("\n").replaceAll("\\\\s*\n", "\\\\"))
                 .map(x =>
                 {
                     if (x.group(1) == null) x.group(4).trim -> ""
                     else x.group(2).trim -> x.group(3).trim
                 })

    source.close()

    var map = LinkedHashMap[String, String]()
    var dups = Set[String]()

    for (item <- items) {
      if (!item._2.isEmpty && map.exists(_._1 == item._1) && !dups.contains(item._1)) {
        dups += item._1

        println(s"Warning: duplicate property '${item._1}'.")
      }

      map += item
    }

    map
  }

  private def selectLines(path: String, specifier: String): LinkedHashMap[String, String] =
    getOrderedProperties(path).filter({ case (p, v) => !p.startsWith("#") && specifier.r.findFirstIn(p).isDefined })

  private def getNextLine(iterator: Iterator[String]): String = {
    var next = ""

    while (iterator.hasNext && next.isEmpty)
      next = iterator.next().trim

    while (iterator.hasNext && next(next.length - 1) == '\\')
      next += iterator.next().trim

    next
  }

  private def addBreaks(s: String, newLine: Boolean = true): String = {
    val r = s.replaceAll("\\\\(?!n)", "\\\\\n")

    if (newLine && r.contains("\n")) r + "\n" else r
  }
}
