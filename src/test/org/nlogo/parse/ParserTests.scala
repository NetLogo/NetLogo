// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api
import org.scalatest.FunSuite

class ParserTests extends FunSuite {

  def bad(s: String, err: String) = {
    val e = intercept[api.CompilerException] {
      Parser.frontEnd(s)
    }
    expectResult(err)(e.getMessage)
  }

  /// duplicate name tests

  test("LetSameNameAsCommandProcedure2") {
    bad("to b let a 5 end  to a end",
      "There is already a procedure called A")
  }
  test("LetSameNameAsReporterProcedure2") {
    bad("to b let a 5 end  to-report a end",
      "There is already a procedure called A")
  }
  test("LetNameSameAsEnclosingCommandProcedureName") {
    bad("to bazort let bazort 5 end",
      "There is already a procedure called BAZORT")
  }
  test("LetNameSameAsEnclosingReporterProcedureName") {
    bad("to-report bazort let bazort 5 report bazort end",
      "There is already a procedure called BAZORT")
  }
  test("SameLocalVariableTwice1") {
    bad("to a1 locals [b b] end",
      "Nothing named LOCALS has been defined")
  }
  test("SameLocalVariableTwice2") {
    bad("to a2 [b b] end",
      "There is already a local variable called B here")
  }
  test("SameLocalVariableTwice3") {
    bad("to a3 let b 5 let b 6 end",
      "There is already a local variable here called B")
  }
  test("SameLocalVariableTwice4") {
    bad("to a4 locals [b] let b 5 end",
      "Nothing named LOCALS has been defined")
  }
  test("SameLocalVariableTwice5") {
    bad("to a5 [b] locals [b] end",
      "Nothing named LOCALS has been defined")
  }
  test("SameLocalVariableTwice6") {
    bad("to a6 [b] let b 5 end",
      "There is already a local variable here called B")
  }

}
