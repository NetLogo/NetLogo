// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import java.io.File
import org.nlogo.api.FileIO.file2String
import org.nlogo.api.AgentKind

object Parser {

  def parseFiles(files: Iterable[File]): Iterable[LanguageTest] =
    (for (f <- files; if (!f.isDirectory))
     yield parseFile(f)).flatten

  def parseFile(f: File): List[LanguageTest] = {
    def preprocessStackTraces(s: String) = s.replace("\\\n  ", "\\n")
    val suiteName =
      if(f.getName == "tests.txt")
        f.getParentFile.getName
      else
        f.getName.replace(".txt", "")
    parseString(suiteName, preprocessStackTraces(file2String(f.getAbsolutePath)))
  }

  def parseString(suiteName: String, s: String): List[LanguageTest] = {
    def split(xs: List[String]): List[LanguageTest] = {
      if (xs.isEmpty) Nil
      else xs.tail.span(_.startsWith(" ")) match {
        case (some, rest) =>
          LanguageTest(suiteName, xs.head.trim, some.map{_.trim}.map(parse)) :: split(rest)
      }
    }
    val lines = s.split("\n").filter(!_.trim.startsWith("#")).filter(!_.trim.isEmpty)
    split(lines.toList)
  }

  val CommandAndErrorRegex = """^([OTPL])>\s+(.*)\s+=>\s+(.*)$""".r
  val ReporterRegex = """^(.*)\s+=>\s+(.*)$""".r
  val CommandRegex = """^([OTPL])>\s+(.*)$""".r
  val OpenModelRegex = """^OPEN>\s+(.*)$""".r

  def agentKind(s: String) = s match {
    case "O" => AgentKind.Observer
    case "T" => AgentKind.Turtle
    case "P" => AgentKind.Patch
    case "L" => AgentKind.Link
    case x => sys.error(s"unrecognized agent kind: $x")
  }

  def parse(line: String): Entry = {
    if (line.startsWith("to ") || line.startsWith("to-report ") || line.startsWith("extensions"))
      Procedure(line)
    else line.trim match {
      case CommandAndErrorRegex(kind, command, err) =>
        if (err.startsWith("ERROR "))
          Command(agentKind(kind), command,
            RuntimeError(err.stripPrefix("ERROR ")))
        else if (err.startsWith("COMPILER ERROR "))
          Command(agentKind(kind), command,
            CompileError(err.stripPrefix("COMPILER ERROR ")))
        else if (err.startsWith("STACKTRACE "))
          Command(agentKind(kind), command,
            StackTrace(err.stripPrefix("STACKTRACE ").replace("\\n", "\n")))
        else
          sys.error("error missing!: " + err)
      case ReporterRegex(reporter, result) =>
        if (result.startsWith("ERROR" ))
          Reporter(reporter,
            RuntimeError(result.stripPrefix("ERROR ")))
        else if (result.startsWith("STACKTRACE" ))
          Reporter(reporter,
            StackTrace(result.stripPrefix("STACKTRACE ").replace("\\n", "\n")))
        else
          Reporter(reporter, Success(result))
      case CommandRegex(kind, command) =>
        Command(agentKind(kind), command)
      case OpenModelRegex(path) => Open(path)
      case _ =>
        throw new IllegalArgumentException(
          s"could not parse: $line")
    }
  }

}
