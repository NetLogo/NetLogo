#!/bin/sh
exec scala -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

import scala.collection.mutable.LinkedHashMap

object Main
{
    def main(args: Array[String]): Unit =
    {
        var mode = ""
        var current = ""
        var current_translated = ""
        var previous = ""
        var previous_translated = ""
        var output = ""
        var reference = ""
        var filter = ""

        var i = 0

        while (i < args.length)
        {
            if (args(i) == "-h")
            {
                return printHelp()
            }

            else if (args(i) == "-g" || args(i) == "-m")
            {
                mode = args(i)
            }

            else if (args(i) == "-c")
            {
                i += 1

                current = args(i)
            }

            else if (args(i) == "-n")
            {
                i += 1

                current_translated = args(i)
            }

            else if (args(i) == "-d")
            {
                i += 1

                previous = args(i)
            }

            else if (args(i) == "-t")
            {
                i += 1

                previous_translated = args(i)
            }

            else if (args(i) == "-o")
            {
                i += 1

                output = args(i)
            }

            else if (args(i) == "-r")
            {
                i += 1

                reference = args(i)
            }

            else if (args(i) == "-f")
            {
                i += 1

                filter = args(i)
            }

            else
            {
                return println(s"Error: invalid flag '${args(i)}'.")
            }

            i += 1
        }

        if (mode == "-g")
        {
            if (current.isEmpty || output.isEmpty)
                return println("Error: incorrect arguments provided for mode 'generate'.")

            val properties =
                if (previous.isEmpty) LinkedHashMap[String, String]()
                else selectLines(previous, filter)
            val properties_translated =
                if (previous_translated.isEmpty) LinkedHashMap[String, String]()
                else selectLines(previous_translated, filter)

            val writer = new java.io.FileWriter(new java.io.File(output))
            val ref_writer =
                if (reference.isEmpty) null
                else new java.io.FileWriter(new java.io.File(reference))

            for ((p, v) <- selectLines(current, filter))
            {
                if (!properties_translated.contains(p) || (properties.contains(p) && properties(p) != v))
                {
                    writer.write(s"Property: ${p}\n")

                    if (properties_translated.contains(p))
                    {
                        writer.write(s"Previous English: ${addBreaks(properties(p))}\n")
                        writer.write(s"Previous Translation: ${addBreaks(properties_translated(p))}\n")
                    }

                    writer.write(s"English: ${addBreaks(v)}\n")
                    writer.write("Translation: \n\n")
                }

                else if (ref_writer != null && properties_translated.contains(p))
                {
                    ref_writer.write(s"Property: ${p}\n")
                    ref_writer.write(s"English: ${addBreaks(v)}\n")
                    ref_writer.write(s"Translation: ${addBreaks(properties_translated(p))}\n\n")
                }
            }

            writer.close()

            if (ref_writer != null) ref_writer.close()
        }

        else if (mode == "-m")
        {
            if (current.isEmpty || output.isEmpty)
                return println("Error: incorrect arguments provided for mode 'merge'.")

            val properties =
                if (previous_translated.isEmpty) LinkedHashMap[String, String]()
                else getOrderedProperties(previous_translated)
            val new_properties =
                if (current_translated.isEmpty) Iterator[String]()
                else scala.io.Source.fromFile(current_translated).getLines()

            while (new_properties.hasNext)
            {
                val next = getNextLine(new_properties)

                if (next.startsWith("Property:"))
                {
                    val name = next.replace("Property:", "").trim

                    if (!name.isEmpty)
                    {
                        if (getNextLine(new_properties).startsWith("Previous:")) getNextLine(new_properties)

                        val translated = getNextLine(new_properties).replace("Translation:", "").trim

                        if (translated.isEmpty) properties.remove(name)
                        else properties(name) = translated
                    }
                }

                else
                {
                    val parts = next.split(" = ", 2)

                    if (parts.length >= 2)
                    {
                        if (parts(1).trim.isEmpty) properties.remove(parts(0).trim)
                        else properties(parts(0).trim) = parts(1).trim
                    }
                }
            }

            val writer = new java.io.FileWriter(new java.io.File(output))

            for ((p, v) <- getOrderedProperties(current))
                if (properties.contains(p))
                    if (p.startsWith("#")) writer.write(p + "\n")
                    else writer.write(s"$p = ${addBreaks(properties(p), false)}\n")

            writer.close()
        }

        else println(s"Error: no mode specified.")
    }

    def printHelp(): Unit =
    {
        println("general")
        println()
        println("-h   display help information")
        println()
        println("mode specification")
        println()
        println("-g   generate new translator file")
        println("-m   merge translator file with existing translations")
        println()
        println("generate mode")
        println()
        println("-c <path>   current english properties file")
        println("-d <path>   outdated english properties file")
        println("-t <path>   outdated translated properties file")
        println("-f <string>   property name filter (regex)")
        println("-o <path>   output file")
        println("-r <path>   reference output file (includes previously translated properties)")
        println()
        println("merge mode")
        println()
        println("-c <path>   current english properties file")
        println("-t <path>   outdated translated properties file")
        println("-n <path>   new translated properties file")
        println("-o <path>   output file")
    }

    def getOrderedProperties(path: String): LinkedHashMap[String, String] =
    {
        val r = "(([\\w.]+)\\s*[=:]\\s*([^\n]+))|(#[^\n]+)".r

        LinkedHashMap(r.findAllMatchIn(scala.io.Source.fromFile(path).getLines().mkString("\n").replaceAll("\\\\s*\n", "\\\\"))
                       .map(x =>
                       {
                            if (x.group(1) == null) x.group(4).trim -> ""
                            else x.group(2).trim -> x.group(3).trim
                       }).toList: _*)
    }

    def selectLines(path: String, specifier: String): LinkedHashMap[String, String] =
        getOrderedProperties(path).filter((p, v) => !p.startsWith("#") && specifier.r.findFirstIn(p).isDefined)

    def getNextLine(iterator: Iterator[String]): String =
    {
        var next = ""

        while (iterator.hasNext && next.isEmpty) next = iterator.next().trim
        while (iterator.hasNext && next(next.length - 1) == '\\') next += iterator.next().trim

        next
    }

    def addBreaks(s: String, newLine: Boolean = true): String =
    {
        val r = s.replaceAll("\\\\(?!n)", "\\\\\n")
        
        if (newLine && r.contains("\n")) r + "\n" else r
    }
}
