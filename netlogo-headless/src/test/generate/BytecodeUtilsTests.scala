// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.scalatest.FunSuite
import org.nlogo.core.LogoList
import org.nlogo.prim._constlist

class BytecodeUtilsTests extends FunSuite {
  test("getUnrejiggeredMethod picks the most specific one") {
    val expected =
      "public org.nlogo.core.LogoList org.nlogo.prim._constlist.report(org.nlogo.nvm.Context)"
    val prim = new _constlist(LogoList.Empty)
    assertResult(expected) {
      BytecodeUtils.getUnrejiggeredMethod(prim).toString
    }
  }
}
