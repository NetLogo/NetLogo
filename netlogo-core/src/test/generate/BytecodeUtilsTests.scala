// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.core.LogoList
import org.nlogo.prim._constlist

class BytecodeUtilsTests extends AnyFunSuite {
  test("getUnrejiggeredMethod picks the most specific one") {
    val expected =
      "public strictfp org.nlogo.core.LogoList org.nlogo.prim._constlist.report(org.nlogo.nvm.Context)"
    val prim = new _constlist(LogoList.Empty)
    assertResult(expected) {
      BytecodeUtils.getUnrejiggeredMethod(prim).toString
    }
  }
}
