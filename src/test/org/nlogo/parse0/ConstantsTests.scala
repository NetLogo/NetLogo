// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse0

import org.scalatest.FunSuite

class ConstantsTests extends FunSuite {
  def isConstant(s: String) = Constants.get(s).isDefined
  def get(s: String) = Constants.get(s).get
  test("BadConstant") { assert(!isConstant("fnord666")) }
  test("OneIsConstant1") { assert(isConstant("FALSE")) }
  test("OneIsConstant2") { assert(isConstant("false")) }
  test("OneConstant1") { expectResult(java.lang.Boolean.FALSE)(get("FALSE")) }
  test("OneConstant2") { expectResult(java.lang.Boolean.FALSE)(get("false")) }
  test("ColorConstant1") { expectResult(105d)(get("blue")) }
  test("ColorConstant2") { expectResult(105d)(get("BLUE")) }
  test("GrayAndGrey") { expectResult(get("grey"))(get("GRAY")) }
}
