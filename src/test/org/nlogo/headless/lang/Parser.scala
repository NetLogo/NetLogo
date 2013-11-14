// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.api, api.AgentKind

object Parser {

  def parse(suiteName: String, s: String): List[LanguageTest] = {
    def split(xs: List[String]): List[LanguageTest] =
      if (xs.isEmpty) Nil
      else xs.tail.span(_.startsWith(" ")) match {
        case (some, rest) =>
          LanguageTest(suiteName, xs.head.trim, some.map{_.trim}.map(parse)) :: split(rest)
      }
    val lines =
      s.split("\n")
        .filter(!_.trim.startsWith("#"))
        .filter(!_.trim.isEmpty)
    split(lines.toList)
  }

  val CommandErrorRegex = """^([OTPL])>\s+(.*)\s+=>\s+(.*)$""".r
  val ReporterRegex = """^(.*)\s+=>\s+(.*)$""".r
  val CommandRegex = """^([OTPL])>\s+(.*)$""".r
  val OpenRegex = """^OPEN>\s+(.*)$""".r

  def agentKind(s: String) = s match {
    case "O" => AgentKind.Observer
    case "T" => AgentKind.Turtle
    case "P" => AgentKind.Patch
    case "L" => AgentKind.Link
    case x => sys.error(s"unrecognized agent kind: $x")
  }

  def parse(line: String): Entry = {
    if (line.split(' ').headOption.exists(s =>
        api.Keywords.isKeyword(s) || s.toUpperCase == "BREED"))
      Declaration(line)
    else line.trim match {
      case CommandErrorRegex(kind, command, err) =>
        if (err.startsWith("ERROR "))
          Command(command, agentKind(kind),
            RuntimeError(err.stripPrefix("ERROR ")))
        else if (err.startsWith("COMPILER ERROR "))
          Command(command, agentKind(kind),
            CompileError(err.stripPrefix("COMPILER ERROR ")))
        else if (err.startsWith("STACKTRACE "))
          Command(command, agentKind(kind),
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
        Command(command, agentKind(kind))
      case OpenRegex(path) => Open(path)
      case _ =>
        throw new IllegalArgumentException(
          s"could not parse: $line")
    }
  }

}
