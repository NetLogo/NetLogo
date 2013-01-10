// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.nvm
import org.nlogo.api.{ DummyExtensionManager, Program }

class AssemblerTests extends FunSuite {
  def compile(keyword: String, source: String): nvm.Procedure = {
    val results = new StructureParser(
      Compiler.Tokenizer2D.tokenize(keyword + " foo " + source + "\nend"), None,
      StructureParser.emptyResults)
      .parse(false)
    expectResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    val tokens =
      new IdentifierParser(results.program, nvm.CompilerInterface.NoProcedures,
                           results.procedures, new DummyExtensionManager)
        .process(results.tokens(procedure).iterator, procedure)
    for (procdef <- new ExpressionParser(procedure).parse(tokens)) {
      procdef.accept(new ArgumentStuffer)
      new Assembler().assemble(procdef)
    }
    procedure
  }

  // these tests focus more on assembly, ignoring argument stuffing.
  // the test strings omit arguments, to make them easier to read & write.
  def test1(source: String) = compile("to", source).code.mkString(" ")
  test("assembleEmptyProcedure") { expectResult("_return")(test1("")) }
  test("assembleSimpleProcedure1") { expectResult("_clearall _return")(test1("ca")) }
  test("assembleSimpleProcedure2") { expectResult("_clearturtles _clearpatches _return")(test1("ct cp")) }
  test("assembleIfElse") {
    expectResult("_ifelse:+4 _fd _fdinternal _goto:6 _bk _fdinternal _return")(
      test1("ifelse timer = 0 [ fd 1 ] [ bk 1 ]"))
  }
  test("assembleAsk") {
    expectResult("_ask:+3 _die _done _return")(
      test1("ask turtles [ die ]"))
  }
  test("assembleWhile") {
    expectResult("_goto:3 _die _die _while:1 _return")(
      test1("while [true] [die die]"))
  }
  test("assembleReporterProcedure") {
    expectResult("_returnreport")(
      compile("to-report", "").code.mkString(" "))
  }

  // these tests are more about checking argument stuffing is working.
  // we check the full text of the procedure dump.
  def test2(source: String): String = {
    val dump = compile("to", source).dump
    val prelude = "procedure FOO:[]{OTPL}:\n"
    assert(dump.startsWith(prelude))
    dump.substring(prelude.length)
  }
  test("stuffEmpty") {
    expectResult("""|[0]_return
              |""".stripMargin.replaceAll("\r\n", "\n"))(
      test2(""))
  }
  test("stuffArithmetic") {
    expectResult("""|[0]_print
           |      _plus
           |        _constdouble:2.0
           |        _constdouble:2.0
           |[1]_return
           |""".stripMargin.replaceAll("\r\n", "\n"))(
      test2("print 2 + 2"))
  }
  test("stuffReporterBlock") {
    expectResult("""|[0]_print
           |      _maxoneof
           |        _turtles
           |        _timer
           |[1]_return
           |""".stripMargin.replaceAll("\r\n", "\n"))(
      test2("print max-one-of turtles [timer]"))
  }
}
