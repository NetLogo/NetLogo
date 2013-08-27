// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

import org.scalatest.FunSuite
import org.nlogo.api
import org.nlogo.util.Femto

class ConstantFolderTests extends FunSuite {

  // cheating here - ST 8/27/13
  val frontEnd = Femto.get[FrontEndInterface](
    "org.nlogo.compile.front.FrontEnd")

  def compile(source: String): String = {
    val (procdef +: _, _) =
      frontEnd.frontEnd(
        "to-report __test report " + source + "\nend")
    procdef.accept(new ConstantFolder)
    procdef.statements.head.head.toString
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
    intercept[api.CompilerException] {
      try compile("1 / 0")
      catch {
        case ex: api.CompilerException =>
          assertResult("Runtime error: Division by zero.")(ex.getMessage)
          throw ex
      }
    }
  }
}
