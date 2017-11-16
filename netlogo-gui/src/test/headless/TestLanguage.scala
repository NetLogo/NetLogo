// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.io.File

import org.nlogo.util.{Utils, SlowTest}
import org.nlogo.core.{ AgentKind, Model, WorldDimensions, WorldDimensions3D }
import org.nlogo.api.{ FileIO, SimpleJobOwner, Version }, FileIO.fileToString
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.headless.test.{ Command, Compile, CompileError, Declaration, LanguageTest,
  NormalMode, Open, Parser, Reporter, RunMode, RuntimeError, Success, StackTrace, TwoDOnly }
import AbstractTestLanguage.TestSpace

import org.scalatest.{FunSuite, Tag}
import org.scalatest.exceptions.TestFailedException

object LanguageTestTag extends Tag("org.nlogo.headless.LanguageTestTag")

object TestLanguage {
  def standardWidgets(version: Version) = {
    import org.nlogo.core.{ Plot, Pen, View }
    val view = View(dimensions =
      if (version.is3D)
        new WorldDimensions3D(-5, 5, -5, 5, -5, 5)
      else
        new WorldDimensions(-5, 5, -5, 5))
    List(
      view,
      Plot(display = Some("plot1"), pens = List(Pen(display = "pen1"), Pen(display = "pen2"))),
      Plot(display = Some("plot2"), pens = List(Pen(display = "pen1"), Pen(display = "pen2"))))
  }

  implicit class LegacyLanguageTest(languageTest: LanguageTest) {
    import languageTest._

    // This is the code that runs each test in both modes, Normal and Run
    def run(r: AbstractTestLanguage.Runner): Unit = {
      import r._
      val nonDecls = entries.filterNot(_.isInstanceOf[Declaration])
      val decls =
        entries.collect{case d: Declaration => d.source}
          .mkString("\n").trim

      if (! nonDecls.exists(e => e.isInstanceOf[Compile] || e.isInstanceOf[Open])) {
        openModel(new Model(code = decls, widgets = standardWidgets(instance.version)))
      } else {
        init()
      }
      nonDecls.foreach {
        case Open(path) => workspace.open(path)
        // need to handle declarations
        case Command(command, kind, result) =>
          try {
            result match {
              case Success(_) =>
                testCommand(command, kind)
              case RuntimeError(message) =>
                testCommandError(command, message, kind)
              case CompileError(message) =>
                testCommandCompilerErrorMessage(command, message, kind)
              case StackTrace(stackTrace) =>
                testCommandErrorStackTrace(command, stackTrace, kind)
            }
          } catch {
            case ex: TestFailedException => throw ex
            case ex: Exception => throw new LanguageTestException(command, ex)
          }
        case Reporter(reporter, result) =>
          try {
            result match {
              case Success(res) =>
                testReporter(reporter, res)
              case RuntimeError(error) =>
                testReporterError(reporter, error)
              case StackTrace(error) =>
                testReporterErrorStackTrace(reporter, error)
              case o => throw new RuntimeException("Invalid result for reporter: " + o)
            }
          } catch {
            case ex: TestFailedException => throw ex
            case ex: Exception => throw new LanguageTestException(reporter, ex)
          }
        case Compile(CompileError(error)) =>
          testCompilerError(new Model(code = decls, widgets = standardWidgets(instance.version)), error)
        case o => throw new RuntimeException("Invalid test case: " + o)
      }
    }
  }
}

// We parse the tests first, then run them.
// Parsing is separate so we can write tests for the parser itself.
abstract class TestLanguage(files: Iterable[File])
extends AbstractTestLanguage(SlowTest.Tag) {

  // do not initialize workspace, that will be done in the Test (above)
  override def initializeRunner(r: Runner): Runner = {
    r
  }

  import TestLanguage._

  def additionalTags: Seq[Tag] = Seq()

  def testSpace(t: LanguageTest): TestSpace = AbstractTestLanguage.spaceFromTest(t)

  for (t: LanguageTest <- TestParser.parseFiles(files)) {
    // use a custom owner so we get fullName into the stack traces
    // we get on the JobThread - ST 1/26/11
    def makeOwner(w: AbstractWorkspace): SimpleJobOwner =
      new SimpleJobOwner(t.fullName, w.world.mainRNG, AgentKind.Observer)

    val tags = Seq(new Tag(t.suiteName){}, new Tag(t.fullName){})

    testInSpaceWithOwner(t.fullName,
      testSpace(t),
      makeOwner _,
      (additionalTags ++ tags): _*) { r =>
      t.run(r)
    }
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
    // We want to allow extenion roots to be symlinked in; we just want to
    // avoid recursive symlinks inside extensions. -BCH 7/23/2016
    val extensionFolders = new File("extensions").listFiles.filter(_.isDirectory)
    extensionFolders.flatMap(filesInDir).filter(_.getName == "tests.txt").iterator
  }
}

class TestCommands extends TestLanguage(TxtsInDir("test/commands")) {
  override def additionalTags = Seq(LanguageTestTag)
}
class TestReporters extends TestLanguage(TxtsInDir("test/reporters")) {
  override def additionalTags = Seq(LanguageTestTag)
}
class TestExtensions extends TestLanguage(ExtensionTestsDotTxt) {
  override def additionalTags = Seq(SlowTest.ExtensionTag)
  override def testSpace(t: LanguageTest): TestSpace = {
    val space = AbstractTestLanguage.spaceFromTest(t)
    if (t.suiteName == "ls") // avoid testing LS in 3D
      space.copy(versions = space.versions.filterNot(_.is3D))
    else
      space
  }
}

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
    parseString(suiteName, preprocessStackTraces(fileToString(f.getAbsolutePath)))
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
        Reporter("[turtle-set self] of turtle 0 = turtles", Success("true"))),
      versionLimit = TwoDOnly)

    assert(tests.head == expectedCommandTest)
    assert(tests.head.modes == List(NormalMode, RunMode))
  }
}

class LanguageTestException (line: String, cause: Exception)
extends Exception("The test errored on the following line: " + line, cause) {
  // This just eliminates the stack trace of LanguageTestException. It still
  // contains the stacktrace of the cause, which is what you care about.
  // -- BCH 11/1/2016
  setStackTrace(Array())
}
