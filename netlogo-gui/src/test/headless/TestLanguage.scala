// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.{FunSuite, Tag}
import org.nlogo.api.{SimpleJobOwner, Version}
import org.nlogo.api.FileIO.file2String
import java.io.File
import org.nlogo.agent.{Turtle, Patch, Link, Observer}
import org.nlogo.util.{Utils, SlowTest}

object LanguageTestTag extends Tag("org.nlogo.headless.LanguageTestTag")

// We parse the tests first, then run them.
// Parsing is separate so we can write tests for the parser itself.
abstract class TestLanguage(files: Iterable[File]) extends FunSuite with SlowTest {
  def additionalTags: Seq[Tag] = Seq()

  for(t:LanguageTest <- TestParser.parseFiles(files); if(t.shouldRun))
    test(t.fullName, (additionalTags ++ Seq(new Tag(t.suiteName){}, new Tag(t.fullName){}, SlowTest.Tag)): _*) {
      t.run
    }
}

trait TestFinder extends Iterable[File]
case class TxtsInDir(dir:String) extends TestFinder {
  override def iterator = new File(dir).listFiles.filter(_.getName.endsWith(".txt")).iterator
}
case object ExtensionTestsDotTxt extends TestFinder {
  def iterator = {
    def filesInDir(parent:File): Iterable[File] =
      parent.listFiles.flatMap{f => if(f.isDirectory && !Utils.isSymlink(f)) filesInDir(f) else List(f)}
    filesInDir(new File("extensions")).filter(_.getName == "tests.txt").iterator
  }
}

class TestCommands extends TestLanguage(TxtsInDir("test/commands")) {
  override def additionalTags = Seq(LanguageTestTag)
}
class TestReporters extends TestLanguage(TxtsInDir("test/reporters")) {
  override def additionalTags = Seq(LanguageTestTag)
}
class TestExtensions extends TestLanguage(ExtensionTestsDotTxt)
class TestModels extends TestLanguage(
  TxtsInDir("models/test")
    .filterNot(_.getName.startsWith("checksums")))

// The output of the parser is lists of instances of these classes:

case class OpenModel(modelPath:String)
case class Proc(content: String)
case class Command(agentType: String, command: String)
case class CommandWithError(agentType: String, command: String, message: String)
case class CommandWithStackTrace(agentType: String, command: String, stackTrace: String)
case class CommandWithCompilerError(agentType: String, command: String, message: String)
case class ReporterWithResult(reporter: String, result: String)
case class ReporterWithError(reporter: String, error: String)
case class ReporterWithStackTrace(reporter: String, stackTrace: String)

// This is the code that runs each test:

case class LanguageTest(suiteName: String, testName: String, commands: List[String]) {
  val lineItems = commands map TestParser.parse
  val (procs, nonProcs) = lineItems.partition(_.isInstanceOf[Proc])
  val proc = Proc(procs.map {_.asInstanceOf[Proc].content}.mkString("\n"))

  // the rules on whether or not the test should be run
  val shouldRun = {
    val envCorrect =
      if (testName.endsWith("_2D")) !Version.is3D
      else if (testName.endsWith("_3D")) Version.is3D
      else true
    val useGenerator = org.nlogo.api.Version.useGenerator
    val generatorCorrect =
      if (testName.startsWith("Generator")) useGenerator
      else if (testName.startsWith("NoGenerator")) !useGenerator
      else true
    envCorrect && generatorCorrect
  }

  def fullName = suiteName + "::" + testName

  // run the test in both modes, Normal and Run
  def run() {
    import AbstractTestLanguage._
    def getAgentClass(a: String) = a match {
      case "O" => classOf[Observer]
      case "T" => classOf[Turtle]
      case "P" => classOf[Patch]
      case "L" => classOf[Link]
      case x => sys.error("unrecognized agent type: " + x)
    }
    class Tester(mode: TestMode) extends AbstractTestLanguage {
      // use a custom owner so we get fullName into the stack traces
      // we get on the JobThread - ST 1/26/11
      override def owner =
        new SimpleJobOwner(fullName, workspace.world.mainRNG, classOf[Observer])
      try {
        init()
        defineProcedures(proc.content)
        nonProcs.foreach {
          case OpenModel(modelPath) => workspace.open(modelPath)
          case Proc(content) =>
            defineProcedures(content)
          case Command(agent, command) =>
            testCommand(command, getAgentClass(agent), mode)
          case CommandWithError(agent, command, message) =>
            testCommandError(command, message, getAgentClass(agent), mode)
          case CommandWithCompilerError(agent, command, message) =>
            testCommandCompilerErrorMessage(command, message, getAgentClass(agent))
          case CommandWithStackTrace(agent, command, stackTrace) =>
            testCommandErrorStackTrace(command, stackTrace, getAgentClass(agent), mode)
          case ReporterWithResult(reporter, result) =>
            testReporter(reporter, result, mode)
          case ReporterWithError(reporter, error) =>
            testReporterError(reporter, error, mode)
          case ReporterWithStackTrace(reporter, error) =>
            testReporterErrorStackTrace(reporter, error, mode)
        }
      }
      finally workspace.dispose()
    }
    new Tester(NormalMode)
    if(!testName.startsWith("*")) new Tester(RunMode)
  }
}

// This is the parser:

object TestParser {
  def parseFiles(files: Iterable[File]): Iterable[LanguageTest] = {
    (for (f <- files; if (!f.isDirectory)) yield parseFile(f)).flatten
  }
  def parseFile(f: File): List[LanguageTest] = {
    def preprocessStackTraces(s:String) = s.replace("\\\n  ", "\\n")
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
  def parse(line: String): AnyRef = {
    if (line.startsWith("to ") || line.startsWith("to-report ") || line.startsWith("extensions"))
      Proc(line)
    else line.trim match {
      case CommandAndErrorRegex(agentType, command, err) =>
        if (err startsWith "ERROR")
          CommandWithError(agentType, command, err.substring("ERROR".length + 1))
        else if (err startsWith "COMPILER ERROR")
          CommandWithCompilerError(agentType, command, err.substring("COMPILER ERROR".length + 1))
        else if (err startsWith "STACKTRACE")
          CommandWithStackTrace(agentType, command, err.substring("STACKTRACE".length + 1).replace("\\n", "\n"))
        else
          sys.error("error missing!: " + err)
      case ReporterRegex(reporter, result) =>
        if (result startsWith "ERROR")
          ReporterWithError(reporter, result.substring("ERROR".length + 1))
        else if (result startsWith "STACKTRACE")
          ReporterWithStackTrace(reporter, result.substring("STACKTRACE".length + 1).replace("\\n", "\n"))
        else
          ReporterWithResult(reporter, result)
      case CommandRegex(agentType, command) =>
        Command(agentType, command)
      case OpenModelRegex(path) => OpenModel(path)
      case _ => sys.error("unrecognized line" + line)
    }
  }
}

// And here are tests for the parser:

class TestParser extends FunSuite {

  // simple regex tests
  test("test command regex", SlowTest.Tag) {
    val TestParser.CommandRegex(agent, command) = "O> crt 1"
    assert(agent === "O")
    assert(command === "crt 1")
  }

  // tests for parsing line items
  val tests = List(
    "O> crt 1" -> Command("O", "crt 1"),

    "O> crt 1 => ERROR some message" -> CommandWithError("O", "crt 1", "some message"),

    "O> crt 1 => COMPILER ERROR some message" -> CommandWithCompilerError("O", "crt 1", "some message"),

    "[turtle-set self] of turtle 0 = turtles => true" -> ReporterWithResult("[turtle-set self] of turtle 0 = turtles", "true"),

    "[link-set self] of link 0 2 => ERROR some message" -> ReporterWithError("[link-set self] of link 0 2", "some message"),

    "to p1 repeat 5 [ crt 1 __ignore p2 ] end" -> Proc("to p1 repeat 5 [ crt 1 __ignore p2 ] end"),

    "to-report p2 foreach [1 2 3] [ report 0 ] end" -> Proc("to-report p2 foreach [1 2 3] [ report 0 ] end"),

    "extensions [ array ]" -> Proc("extensions [ array ]")
    )
  for((input, output) <- tests)
    test("parse: " + input, SlowTest.Tag) {
      assert(TestParser.parse(input) === output)
    }

  // test entire path
  test("parse a simple test", SlowTest.Tag) {
    val code = """
TurtleSet_2D
  O> crt 1
  [turtle-set self] of turtle 0 = turtles => true
"""
    val tests = TestParser.parseString("test", code)

    val expectedCommandTest = LanguageTest("test", "TurtleSet_2D",
      List("O> crt 1", "[turtle-set self] of turtle 0 = turtles => true"))

    assert(tests.toString === "List(LanguageTest(test,TurtleSet_2D,List(O> crt 1, [turtle-set self] of turtle 0 = turtles => true)))")
  }
}
