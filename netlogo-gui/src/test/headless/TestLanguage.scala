// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.core.{ AgentKind, Model }
import org.nlogo.headless.test.{ Parser, LanguageTest, Open, Declaration, Command,
  Reporter, Compile, Success, CompileError, RuntimeError, StackTrace }
import org.scalatest.{FunSuite, Tag}
import org.nlogo.api.{SimpleJobOwner, Version}
import org.nlogo.api.FileIO.file2String
import java.io.File
import org.nlogo.util.{Utils, SlowTest}

object LanguageTestTag extends Tag("org.nlogo.headless.LanguageTestTag")

object TestLanguage {
  val StandardWidgets = {
    import org.nlogo.core.{ Plot, Pen, View }
    List(
      View.square(5),
      Plot(display = "plot1", pens = List(Pen(display = "pen1"), Pen(display = "pen2"))),
      Plot(display = "plot2", pens = List(Pen(display = "pen1"), Pen(display = "pen2"))))
  }
  implicit class LegacyLanguageTest(languageTest: LanguageTest) {
    import languageTest._
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

    // This is the code that runs each test in both modes, Normal and Run
    def run(): Unit = {
      import AbstractTestLanguage._
      class Tester(mode: TestMode) extends AbstractTestLanguage {
        // use a custom owner so we get fullName into the stack traces
        // we get on the JobThread - ST 1/26/11
        override def owner =
          new SimpleJobOwner(fullName, workspace.world.mainRNG, AgentKind.Observer)
        try {
          init()
          val nonDecls = entries.filterNot(_.isInstanceOf[Declaration])
          val decls =
            entries.collect{case d: Declaration => d.source}
              .mkString("\n").trim

          if (! nonDecls.exists(e => e.isInstanceOf[Compile] || e.isInstanceOf[Open])) {
            openModel(new Model(code = decls, widgets = StandardWidgets))
          }
          nonDecls.foreach {
            case Open(path) =>
              workspace.open(path)
            // need to handle declarations
            case Command(command, kind, result) =>
              result match {
                case Success(_) =>
                  testCommand(command, kind, mode)
                case RuntimeError(message) =>
                  testCommandError(command, message, kind, mode)
                case CompileError(message) =>
                  testCommandCompilerErrorMessage(command, message, kind)
                case StackTrace(stackTrace) =>
                  testCommandErrorStackTrace(command, stackTrace, kind, mode)
              }
            case Reporter(reporter, result) =>
              result match {
                case Success(res) =>
                  testReporter(reporter, res, mode)
                case RuntimeError(error) =>
                  testReporterError(reporter, error, mode)
                case StackTrace(error) =>
                  testReporterErrorStackTrace(reporter, error, mode)
                case o => throw new RuntimeException("Invalid result for reporter: " + o)
              }
            case Compile(CompileError(error)) =>
              testCompilerError(new Model(code = decls, widgets = StandardWidgets), error)
            case o => throw new RuntimeException("Invalid test case: " + o)
          }
        }
        finally workspace.dispose()
      }
      new Tester(NormalMode)
      if(!testName.startsWith("*")) new Tester(RunMode)
    }
  }
}
// We parse the tests first, then run them.
// Parsing is separate so we can write tests for the parser itself.
abstract class TestLanguage(files: Iterable[File]) extends FunSuite with SlowTest {
  import TestLanguage._
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
    Parser.parse(suiteName, s)
  }
}

// And here are tests for the parser:

class TestParser extends FunSuite {

  // most of parsing tests are in headless, this is just a sanity check
  test("parse a simple test", SlowTest.Tag) {
    val code = """
TurtleSet_2D
  O> crt 1
  [turtle-set self] of turtle 0 = turtles => true
"""
    val tests = TestParser.parseString("test", code)

    val expectedCommandTest = LanguageTest("test", "TurtleSet_2D",
      List(
        Command("crt 1"),
        Reporter("[turtle-set self] of turtle 0 = turtles", Success("true"))))

    assert(tests.toString == "List(LanguageTest(test,TurtleSet_2D,List(Command(crt 1,Observer,Success()), Reporter([turtle-set self] of turtle 0 = turtles,Success(true)))))")
  }
}
