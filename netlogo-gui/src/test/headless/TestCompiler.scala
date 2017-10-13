// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.{ FunSuite, Tag }
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.util.{ ThreeDTag, TwoDTag }

class TestCompiler extends FunSuite {

  class TestEnv(val is3D: Boolean) {
    var _workspace: HeadlessWorkspace = _

    def workspace: HeadlessWorkspace = {
      if (_workspace == null) {
        _workspace = HeadlessWorkspace.newInstance(is3D)
      }
      _workspace
    }

    def cleanup(): Unit = {
      if (_workspace != null)
        workspace.dispose()
    }

    def declare(source:String) {
      initForTesting(5, source)
    }

    def initForTesting(size: Int, source: String) {
      workspace.initForTesting(is3D, size, source)
    }

    def declareBad(source:String,expectedError:String) {
      val exception = intercept[CompilerException] {
        declare(source)
      }
      assertResult(expectedError)(exception.getMessage)
    }

    def badCommand(command:String,expectedError:String) {
      val exception = intercept[CompilerException] {
        workspace.command(command)
      }
      assertResult(expectedError)(exception.getMessage)
    }
  }

  def compilerTest(name: String)(f: TestEnv => Unit): Unit = {
    def runTest(suffix: String, tag: Tag, env: => TestEnv): Unit = {
      test(name + suffix, tag) {
        try {
          f(env)
        }
        finally {
          env.cleanup()
        }
      }
    }
    runTest(" (2D)", TwoDTag, new TestEnv(false))
    runTest(" (3D)", ThreeDTag, new TestEnv(true))
  }

  compilerTest("RunUnknownProcedure") { e =>
    import e._
    badCommand("foo 5",
               I18N.errors.getN("compiler.LocalsVisitor.notDefined", "FOO"))
  }

  compilerTest("RunWithUnknownArgument") { e =>
    import e._
    badCommand("crt foo",
               I18N.errors.getN("compiler.LocalsVisitor.notDefined", "FOO"))
  }

  compilerTest("GetUnknownVariable") { e =>
    import e._
    badCommand("let a foo",
               I18N.errors.getN("compiler.LocalsVisitor.notDefined", "FOO"))
  }

  compilerTest("SetUnknownVariable") { e =>
    import e._
    badCommand("set foo 5",
               I18N.errors.getN("compiler.LocalsVisitor.notDefined", "FOO"))
  }

  compilerTest("LetSameVariableTwice1") { e =>
    import e._
    badCommand("let a 5 let a 6",
               "There is already a local variable here called A")
  }
  compilerTest("LetSameVariableTwice2") { e =>
    import e._
    badCommand("let a 5 ask patches [ let a 6 ]",
               "There is already a local variable here called A")
  }
  compilerTest("LetSameNameAsCommandProcedure1") { e =>
    import e._
    declare("to a end")
    badCommand("let a 5",
               "There is already a procedure called A")
  }
  compilerTest("LetSameNameAsCommandProcedure2") { e =>
    import e._
    declareBad("to b let a 5 end  to a end",
               "There is already a procedure called A")
  }
  compilerTest("LetSameNameAsReporterProcedure1") { e =>
    import e._
    declare("to-report a end")
    badCommand("let a 5",
               "There is already a procedure called A")
  }
  compilerTest("LetSameNameAsReporterProcedure2") { e =>
    import e._
    declareBad("to b let a 5 end  to-report a end",
               "There is already a procedure called A")
  }
  compilerTest("LetSameNameAsGlobal") { e =>
    import e._
    declare("globals [glob1]")
    badCommand("let glob1 5",
               "There is already a global variable called GLOB1")
  }
  compilerTest("LetSameNameAsBreed") { e =>
    import e._
    declare("breed [mice mouse]")
    badCommand("let mice 5",
               "There is already a breed called MICE")
  }
  compilerTest("LetSameNameAsTurtleVariable") { e =>
    import e._
    declare("turtles-own [tvar]")
    badCommand("let tvar 5",
               "There is already a turtle variable called TVAR")
  }
  compilerTest("LetSameNameAsBreedVariable") { e =>
    import e._
    declare("breed [mice mouse] mice-own [fur]")
    badCommand("let fur 5",
               "There is already a MICE-OWN variable called FUR")
  }
  compilerTest("LetSameNameAsPrimitiveCommand") { e =>
    import e._
    badCommand("let fd 5",
               "There is already a primitive command called FD")
  }
  compilerTest("LetSameNameAsPrimitiveReporter1") { e =>
    import e._
    badCommand("let timer 5",
               "There is already a primitive reporter called TIMER")
  }
  compilerTest("LetSameNameAsPrimitiveReporter2") { e =>
    import e._
    badCommand("let sin 5",
               "There is already a primitive reporter called SIN")
  }
  compilerTest("LetShadowsLet") { e =>
    import e._
    badCommand("let x 4 ask patches [ let x 0 ]",
               "There is already a local variable here called X")
  }
  compilerTest("LetNameSameAsEnclosingCommandProcedureName") { e =>
    import e._
    declareBad("to bazort let bazort 5 end",
               "There is already a procedure called BAZORT")
  }
  compilerTest("LetNameSameAsEnclosingReporterProcedureName") { e =>
    import e._
    declareBad("to-report bazort let bazort 5 report bazort end",
               "There is already a procedure called BAZORT")
  }
  compilerTest("SameLocalVariableTwice1") { e =>
    import e._
    declareBad("to a1 locals [b b] end",
               I18N.errors.getN("compiler.LocalsVisitor.notDefined", "LOCALS"))
  }
  compilerTest("SameLocalVariableTwice2") { e =>
    import e._
    declareBad("to a2 [b b] end",
               "There is already a local variable called B here")
  }
  compilerTest("SameLocalVariableTwice3") { e =>
    import e._
    declareBad("to a3 let b 5 let b 6 end",
               "There is already a local variable here called B")
  }
  compilerTest("SameLocalVariableTwice4") { e =>
    import e._
    declareBad("to a4 locals [b] let b 5 end",
               I18N.errors.getN("compiler.LocalsVisitor.notDefined", "LOCALS"))
  }
  compilerTest("SameLocalVariableTwice5") { e =>
    import e._
    declareBad("to a5 [b] locals [b] end",
               I18N.errors.getN("compiler.LocalsVisitor.notDefined", "LOCALS"))
  }
  compilerTest("SameLocalVariableTwice6") { e =>
    import e._
    declareBad("to a6 [b] let b 5 end",
               "There is already a local variable here called B")
  }
  compilerTest("NonAsciiChars") { e =>
    import e._
    badCommand("blah " + 8211.toChar + " blah ",
               "This non-standard character is not allowed.")
  }
  compilerTest("BreedOwnsConflict") { e =>
    import e._
    declareBad("undirected-link-breed [edges edge]\n" +
               "breed [nodes node]\n" +
               "breed [foos foo]\n" +
               "edges-own [weight]\n" +
               "nodes-own [weight]\n" +
               "foos-own [weight] ",
               "There is already a EDGES-OWN variable called WEIGHT")
  }

  compilerTest("BreedOwnsNoConflict") { e =>
    import e._
    initForTesting(5,
                   "undirected-link-breed [edges edge]\n" +
                   "breed [nodes node]\n" +
                   "breed [foos foo]\n" +
                   "edges-own [lweight]\n" +
                   "nodes-own [weight]\n" +
                   "foos-own [weight]")
  }

  /// isReporter

  val reporters = Seq("3", "[]", "[", "((5))", "timer", "glob1")
  val nonReporters = Seq("", ";", " ; ", "ca", "((ca))", "5984783478344387487348734")
  for(x <- reporters)
    compilerTest("is a reporter: '" + x + "'") { e =>
      import e._
      initForTesting(5, HeadlessWorkspace.TestDeclarations)
      assertResult(true) { workspace.compilerServices.isReporter(x) }
    }
  for(x <- nonReporters)
    compilerTest("isn't a reporter: '" + x + "'") { e =>
      import e._
      initForTesting(5, HeadlessWorkspace.TestDeclarations)
      assertResult(false) { workspace.compilerServices.isReporter(x) }
    }

  compilerTest("isReporter on user-defined procedures") { e =>
    import e._
    initForTesting(5, "to foo end to-report bar [] report 5 end")
    assertResult(false) { workspace.compilerServices.isReporter("foo") }
    assertResult(true) { workspace.compilerServices.isReporter("bar") }
  }

}
