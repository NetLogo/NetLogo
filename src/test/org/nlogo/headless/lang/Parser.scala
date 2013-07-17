// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.scalatest.FunSuite
import java.io.File
import org.nlogo.api.FileIO.file2String

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
          LanguageTest(suiteName, xs.head.trim, some.map {_.trim}) :: split(rest)
      }
    }
    val lines = s.split("\n").filter(!_.trim.startsWith("#")).filter(!_.trim.isEmpty)
    split(lines.toList)
  }

  val CommandAndErrorRegex = """^([OTPL])>\s+(.*)\s+=>\s+(.*)$""".r
  val ReporterRegex = """^(.*)\s+=>\s+(.*)$""".r
  val CommandRegex = """^([OTPL])>\s+(.*)$""".r
  val OpenModelRegex = """^OPEN>\s+(.*)$""".r

  def parse(line: String): Entry = {
    if (line.startsWith("to ") || line.startsWith("to-report ") || line.startsWith("extensions"))
      Proc(line)
    else line.trim match {
      case CommandAndErrorRegex(agentKind, command, err) =>
        if (err startsWith "ERROR")
          CommandWithError(agentKind, command, err.substring("ERROR".length + 1))
        else if (err startsWith "COMPILER ERROR")
          CommandWithCompilerError(agentKind, command, err.substring("COMPILER ERROR".length + 1))
        else if (err startsWith "STACKTRACE")
          CommandWithStackTrace(agentKind, command, err.substring("STACKTRACE".length + 1).replace("\\n", "\n"))
        else
          sys.error("error missing!: " + err)
      case ReporterRegex(reporter, result) =>
        if (result startsWith "ERROR")
          ReporterWithError(reporter, result.substring("ERROR".length + 1))
        else if (result startsWith "STACKTRACE")
          ReporterWithStackTrace(reporter, result.substring("STACKTRACE".length + 1).replace("\\n", "\n"))
        else
          ReporterWithResult(reporter, result)
      case CommandRegex(agentKind, command) =>
        Command(agentKind, command)
      case OpenModelRegex(path) => OpenModel(path)
      case _ => sys.error("unrecognized line" + line)
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
      Command("O", "crt 1")),
    ("O> crt 1 => ERROR some message",
      CommandWithError("O", "crt 1", "some message")),
    ("O> crt 1 => COMPILER ERROR some message",
      CommandWithCompilerError("O", "crt 1", "some message")),
    ("[turtle-set self] of turtle 0 = turtles => true",
      ReporterWithResult("[turtle-set self] of turtle 0 = turtles", "true")),
    ("[link-set self] of link 0 2 => ERROR some message",
      ReporterWithError("[link-set self] of link 0 2", "some message")),
    ("to p1 repeat 5 [ crt 1 __ignore p2 ] end",
      Proc("to p1 repeat 5 [ crt 1 __ignore p2 ] end")),
    ("to-report p2 foreach [1 2 3] [ report 0 ] end",
      Proc("to-report p2 foreach [1 2 3] [ report 0 ] end")),
    ("extensions [ array ]",
      Proc("extensions [ array ]"))
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
        List("O> crt 1", "[turtle-set self] of turtle 0 = turtles => true")))
    assert(tests.toString === expectedOutputs.toString)
  }

}
