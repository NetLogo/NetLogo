// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.scalatest.FunSuite
import java.io.File
import org.nlogo.api.FileIO.file2String
import org.nlogo.api.AgentKind

// Parsing is separate for clarity, and so we can write tests for the parser itself.

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
    case x => sys.error("unrecognized agent kind: " + x)
  }

  def parse(line: String): Entry = {
    if (line.startsWith("to ") || line.startsWith("to-report ") || line.startsWith("extensions"))
      Procedure(line)
    else line.trim match {
      case CommandAndErrorRegex(kind, command, err) =>
        if (err startsWith "ERROR")
          Command(agentKind(kind), command,
            RuntimeError(err.substring("ERROR".length + 1)))
        else if (err startsWith "COMPILER ERROR")
          Command(agentKind(kind), command,
            CompileError(err.substring("COMPILER ERROR".length + 1)))
        else if (err startsWith "STACKTRACE")
          Command(agentKind(kind), command,
            StackTrace(err.substring("STACKTRACE".length + 1).replace("\\n", "\n")))
        else
          sys.error("error missing!: " + err)
      case ReporterRegex(reporter, result) =>
        if (result startsWith "ERROR")
          Reporter(reporter,
            RuntimeError(result.substring("ERROR".length + 1)))
        else if (result startsWith "STACKTRACE")
          Reporter(reporter,
            StackTrace(result.substring("STACKTRACE".length + 1).replace("\\n", "\n")))
        else
          Reporter(reporter, Success(result))
      case CommandRegex(kind, command) =>
        Command(agentKind(kind), command)
      case OpenModelRegex(path) => Open(path)
      case _ =>
        throw new IllegalArgumentException(
          "could not parse: " + line)
    }
  }
}

// And here are tests for the parser:

class ParserTests extends FunSuite {

  // simple regex tests
  test("test command regex") {
    val Parser.CommandRegex(agent, command) = "O> crt 1"
    assert(agent === "O")
    assert(command === "crt 1")
  }

  // tests for parsing line items.
  // (pending resolution of https://issues.scala-lang.org/browse/SI-6723
  // we avoid the `a -> b` syntax in favor of `(a, b)` - ST 1/3/13)
  val tests = List(
    ("O> crt 1",
      Command(AgentKind.Observer, "crt 1")),
    ("O> crt 1 => ERROR some message",
      Command(AgentKind.Observer, "crt 1", RuntimeError("some message"))),
    ("O> crt 1 => COMPILER ERROR some message",
      Command(AgentKind.Observer, "crt 1", CompileError("some message"))),
    ("[turtle-set self] of turtle 0 = turtles => true",
      Reporter("[turtle-set self] of turtle 0 = turtles", Success("true"))),
    ("[link-set self] of link 0 2 => ERROR some message",
      Reporter("[link-set self] of link 0 2", RuntimeError("some message"))),
    ("to p1 repeat 5 [ crt 1 __ignore p2 ] end",
      Procedure("to p1 repeat 5 [ crt 1 __ignore p2 ] end")),
    ("to-report p2 foreach [1 2 3] [ report 0 ] end",
      Procedure("to-report p2 foreach [1 2 3] [ report 0 ] end")),
    ("extensions [ array ]",
      Procedure("extensions [ array ]"))
  )

  for((input, output) <- tests)
    test("parse: " + input) {
      assert(Parser.parse(input) === output)
    }

  // test entire path
  test("parse a simple test") {
    val code = """
               |TurtleSet
               |  O> crt 1
               |  [turtle-set self] of turtle 0 = turtles => true""".stripMargin
    val tests = Parser.parseString("test", code)
    val expectedOutputs =
      List(LanguageTest("test", "TurtleSet",
        List(
          Parser.parse("O> crt 1"),
          Parser.parse("[turtle-set self] of turtle 0 = turtles => true"))))
    assert(tests.toString === expectedOutputs.toString)
  }

}
