// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

import org.nlogo.core.{CompilerException, Femto}
import org.scalatest.FunSuite
import org.nlogo.api

class ConstantFolderTests extends FunSuite {

  def compile(source: String): String = {
    val procdef +: _ =
      Scaffold.apply(
        "to-report __test report " + source + "\nend")
    procdef.accept(new ConstantFolder)
    procdef.statements.stmts.head.args.head.toString
  }

  /// not pure
  test("NonConstant") { assertResult("_timer[]")(compile("timer")) }
  test("NestedNonConstant") {
    assertResult("_plus[_const:1[], _timer[]]")(
      compile("1 + timer"))
  }

  /// pure
  test("Addition") { assertResult("_constdouble:4.0[]")(compile("2 + 2")) }
  test("Nesting") { assertResult("_constdouble:19.0[]")(compile("2 + 3 * 4 + 5")) }

  /// runtime errors
  test("Error") {
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
