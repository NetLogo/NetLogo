// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager}
import org.nlogo.core.Program
import org.nlogo.core.CompilerException
import org.nlogo.nvm.Procedure

class ConstantFolderTests extends FunSuite {

  def compile(source: String): String = {
    val program = Program.empty()
    val results = TestHelper.structureParse(s"to-report __test report $source \nend", program)
    assertResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    val tokens =
      new IdentifierParser(program, java.util.Collections.emptyMap[String, Procedure], results.procedures)
        .process(results.tokens(procedure).iterator, procedure)
    val procdef = new ExpressionParser(procedure).parse(tokens).head
    procdef.accept(new ConstantFolder)
    procdef.statements.body.head.args.head.toString
  }

  /// not pure
  test("testNonConstant") { assertResult("_timer[]")(compile("timer")) }
  test("testNestedNonConstant") {
    assertResult("_plus[_constdouble:1.0[], _timer[]]")(
      compile("1 + timer"))
  }

  /// pure, easy
  test("testNumber") { assertResult("_constdouble:1.0[]")(compile("1")) }
  test("testBoolean") { assertResult("_constboolean:true[]")(compile("true")) }
  test("testList") { assertResult("_constlist:[1 2 3][]")(compile("[1 2 3]")) }
  test("testString") { assertResult("_conststring:\"foo\"[]")(compile("\"foo\"")) }
  test("testNobody") { assertResult("_nobody[]")(compile("nobody")) }

  /// pure, harder
  test("testAddition") { assertResult("_constdouble:4.0[]")(compile("2 + 2")) }
  test("testNesting") { assertResult("_constdouble:19.0[]")(compile("2 + 3 * 4 + 5")) }

  /// runtime errors
  test("testError") {
    // hmm, is there an easier way in ScalaTest to check the message in an exception? - ST 4/2/11
    intercept[CompilerException] {
      try compile("1 / 0")
      catch {
        case ex: CompilerException =>
          assertResult("Runtime error: Division by zero.")(ex.getMessage)
          throw ex
      }
    }
  }
}
