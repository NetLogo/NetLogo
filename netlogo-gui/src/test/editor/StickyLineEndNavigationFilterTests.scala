// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import org.scalatest.FunSuite

class StickyLineEndNavigationFilterTests extends FunSuite {
  trait Helper {
    var text: String = ""
    lazy val ea = new DummyEditorArea(text)
    val logic = new StickyLineEndNavigationFilter.Logic()
  }

  test("moving between full lines switches as normal") { new Helper {
    text = "abc\ndef"
    assertResult(5)(logic.move(ea, 0, 5))
  } }

  test("moving from a full line to an whitespace-only line moves to the end of that line") { new Helper {
    text = "abc\n   "
    assertResult(7)(logic.move(ea, 0, 5))
  } }

  test("moving from a full line to a whitespace-only line, back to a full line preserves line offset") { new Helper {
    text = "abc\n   \ndef"
    logic.move(ea, 0, 5)
    assertResult(8)(logic.move(ea, 7, 11))
  } }

  test("ignores line offset when moving from a different position than the prior one") { new Helper {
    text = "abc\n   \ndef"
    assertResult(7)(logic.move(ea, 0, 5))
    assertResult(10)(logic.move(ea, 6, 10))
  } }

  test("offset preservation does not go past the end of a non-empty line") { new Helper {
    text = "abc\n   \nd\nhij"
    assertResult(7)(logic.move(ea, 2, 6))
    assertResult(9)(logic.move(ea, 7, 9))
  } }

  test("offset preservation continues across multiple blank lines") { new Helper {
    text = "abc\n   \n   \ndef"
    assertResult(7)(logic.move(ea, 2, 6))
    assertResult(11)(logic.move(ea, 7, 11))
    assertResult(14)(logic.move(ea, 11, 15))
  } }
}
