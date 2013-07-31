// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse0

import org.scalatest.FunSuite

class ConstantsTests extends FunSuite {
  def isConstant(s: String) = Constants.get(s).isDefined
  def get(s: String) = Constants.get(s).get
  test("BadConstant") { assert(!isConstant("fnord666")) }
  test("OneIsConstant1") { assert(isConstant("FALSE")) }
  test("OneIsConstant2") { assert(isConstant("false")) }
  test("OneConstant1") { assertResult(java.lang.Boolean.FALSE)(get("FALSE")) }
  test("OneConstant2") { assertResult(java.lang.Boolean.FALSE)(get("false")) }
  test("ColorConstant1") { assertResult(105d)(get("blue")) }
  test("ColorConstant2") { assertResult(105d)(get("BLUE")) }
  test("GrayAndGrey") { assertResult(get("grey"))(get("GRAY")) }
}
