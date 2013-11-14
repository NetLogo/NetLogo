// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

import org.scalatest.FunSuite
import org.nlogo.nvm
import org.nlogo.util.Femto

class AssemblerTests extends FunSuite {

  // cheating here - ST 8/27/13
  val frontEnd = Femto.get[FrontEndInterface](
    "org.nlogo.compile.front.FrontEnd")

  def compile(keyword: String, source: String): nvm.Procedure = {
    val (defs, results) =
      frontEnd.frontEnd(
        keyword + " foo " + source + "\nend")
    assertResult(1)(results.procedures.size)
    for (procdef <- defs) {
      procdef.accept(new ArgumentStuffer)
      new Assembler().assemble(procdef)
    }
    results.procedures.values.head
  }

  // these tests focus more on assembly, ignoring argument stuffing.
  // the test strings omit arguments, to make them easier to read & write.
  def test1(source: String) = compile("to", source).code.mkString(" ")
  test("assembleEmptyProcedure") { assertResult("_return")(test1("")) }
  test("assembleSimpleProcedure1") { assertResult("_clearall _return")(test1("ca")) }
  test("assembleSimpleProcedure2") { assertResult("_clearturtles _clearpatches _return")(test1("ct cp")) }
  test("assembleIfElse") {
    assertResult("_ifelse:+4 _fd _fdinternal _goto:6 _bk _fdinternal _return")(
      test1("ifelse timer = 0 [ fd 1 ] [ bk 1 ]"))
  }
  test("assembleAsk") {
    assertResult("_ask:+3 _die _done _return")(
      test1("ask turtles [ die ]"))
  }
  test("assembleWhile") {
    assertResult("_goto:3 _die _die _while:1 _return")(
      test1("while [true] [die die]"))
  }
  test("assembleReporterProcedure") {
    assertResult("_returnreport")(
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
    assertResult("""|[0]_return
              |""".stripMargin.replaceAll("\r\n", "\n"))(
      test2(""))
  }
  test("stuffArithmetic") {
    assertResult("""|[0]_print
           |      _plus
           |        _constdouble:2.0
           |        _constdouble:2.0
           |[1]_return
           |""".stripMargin.replaceAll("\r\n", "\n"))(
      test2("print 2 + 2"))
  }
  test("stuffReporterBlock") {
    assertResult("""|[0]_print
           |      _maxoneof
           |        _turtles
           |        _timer
           |[1]_return
           |""".stripMargin.replaceAll("\r\n", "\n"))(
      test2("print max-one-of turtles [timer]"))
  }
}
