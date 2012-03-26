// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.{ FunSuite, OneInstancePerTest, BeforeAndAfterEach }
import org.nlogo.api.{ CompilerException }

class TestCompiler extends FunSuite with OneInstancePerTest with BeforeAndAfterEach {

  var workspace: HeadlessWorkspace = _
  override def beforeEach() { workspace = HeadlessWorkspace.newInstance }
  override def afterEach() { workspace.dispose() }

  def declare(source:String) {
    workspace.initForTesting(-5,5,-5,5,source)
  }
  def declareBad(source:String,expectedError:String) {
    val exception = intercept[CompilerException] {
      declare(source)
    }
    expect(expectedError)(exception.getMessage)
  }
  def badCommand(command:String,expectedError:String) {
    val exception = intercept[CompilerException] {
      workspace.command(command)
    }
    expect(expectedError)(exception.getMessage)
  }

  test("LetSameVariableTwice1") {
    badCommand("let a 5 let a 6",
               "There is already a local variable called A here")
  }
  test("LetSameVariableTwice2") {
    badCommand("let a 5 ask patches [ let a 6 ]",
               "There is already a local variable called A here")
  }
  test("LetSameNameAsCommandProcedure1") {
    declare("to a end")
    badCommand("let a 5",
               "There is already a procedure with that name")
  }
  test("LetSameNameAsCommandProcedure2") {
    declareBad("to b let a 5 end  to a end",
               "There is already a local variable called A in the B procedure")
  }
  test("LetSameNameAsReporterProcedure1") {
    declare("to-report a end")
    badCommand("let a 5",
               "There is already a procedure with that name")
  }
  test("LetSameNameAsReporterProcedure2") {
    declareBad("to b let a 5 end  to-report a end",
               "There is already a local variable called A in the B procedure")
  }
  test("LetSameNameAsGlobal") {
    declare("globals [glob1]")
    badCommand("let glob1 5",
               "There is already a global variable called GLOB1")
  }
  test("LetSameNameAsBreed") {
    declare("breed [mice mouse]")
    badCommand("let mice 5",
               "There is already a breed called MICE")
  }
  test("LetSameNameAsTurtleVariable") {
    declare("turtles-own [tvar]")
    badCommand("let tvar 5",
               "There is already a turtle variable called TVAR")
  }
  test("LetSameNameAsBreedVariable") {
    declare("breed [mice mouse] mice-own [fur]")
    badCommand("let fur 5",
               "You already defined FUR as a MICE variable")
  }
  test("LetSameNameAsPrimitiveCommand") {
    badCommand("let fd 5",
               "Expected variable name here")
  }
  test("LetSameNameAsPrimitiveReporter1") {
    badCommand("let timer 5",
               "Expected variable name here")
  }
  test("LetSameNameAsPrimitiveReporter2") {
    badCommand("let sin 5",
               "Expected variable name here")
  }
  test("LetShadowsLet") {
    badCommand("let x 4 ask patches [ let x 0 ]",
               "There is already a local variable called X here")
  }
  test("LetNameSameAsEnclosingCommandProcedureName") {
    declareBad("to bazort let bazort 5 end",
               "There is already a procedure with that name")
  }
  test("LetNameSameAsEnclosingReporterProcedureName") {
    declareBad("to-report bazort let bazort 5 report bazort end",
               "There is already a procedure with that name")
  }
  test("SameLocalVariableTwice1") {
    declareBad("to a1 locals [b b] end",
               "Nothing named LOCALS has been defined")
  }
  test("SameLocalVariableTwice2") {
    declareBad("to a2 [b b] end",
               "The name B is already defined")
  }
  test("SameLocalVariableTwice3") {
    declareBad("to a3 let b 5 let b 6 end",
               "There is already a local variable called B here")
  }
  test("SameLocalVariableTwice4") {
    declareBad("to a4 locals [b] let b 5 end",
               "Nothing named LOCALS has been defined")
  }
  test("SameLocalVariableTwice5") {
    declareBad("to a5 [b] locals [b] end",
               "Nothing named LOCALS has been defined")
  }
  test("SameLocalVariableTwice6") {
    declareBad("to a6 [b] let b 5 end",
               "There is already a local variable called B here")
  }
  test("NonAsciiChars") {
    badCommand("blah " + 8211.toChar + " blah ",
               "This non-standard character is not allowed.")
  }
  test("BreedOwnsConflict") {
    declareBad("undirected-link-breed [edges edge]\n" +
               "breed [nodes node]\n" +
               "breed [foos foo]\n" +
               "edges-own [weight]\n" +
               "nodes-own [weight]\n" +
               "foos-own [weight] ",
               "You already defined WEIGHT as a EDGES variable")
  }
  test("BreedOwnsNoConflict") {
    workspace.initForTesting(-5,5,-5,5,
                             "undirected-link-breed [edges edge]\n" +
                             "breed [nodes node]\n" +
                             "breed [foos foo]\n" +
                             "edges-own [lweight]\n" +
                             "nodes-own [weight]\n" +
                             "foos-own [weight]")
  }

  /// isReporter

  val reporters = Seq("3", "[]", "[", "((5))", "timer", "glob1")
  val nonReporters = Seq("", ";", " ; ", "ca", "((ca))",
                         "5984783478344387487348734", "gkhjfghkjfhjkg")
  for(x <- reporters)
    test("is a reporter: '" + x + "'") {
      workspace.initForTesting(5, HeadlessWorkspace.TestDeclarations)
      expect(true) { workspace.isReporter(x) }
    }
  for(x <- nonReporters)
    test("isn't a reporter: '" + x + "'") {
      workspace.initForTesting(5, HeadlessWorkspace.TestDeclarations)
      expect(false) { workspace.isReporter(x) }
    }

  test("isReporter on user-defined procedures") {
    workspace.initForTesting(5, "to foo end to-report bar [] report 5 end")
    import collection.JavaConverters._
    expect(false) { workspace.isReporter("foo") }
    expect(true) { workspace.isReporter("bar") }
  }

}
