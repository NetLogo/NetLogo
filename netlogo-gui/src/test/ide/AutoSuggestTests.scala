// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import org.scalatest.FunSuite

class AutoSuggestTests extends FunSuite {
  val autoSuggest = new AutoSuggest()
  test("empty"){
    val testList = Seq()
    assertResult(testList)(autoSuggest.getSuggestions("zzzzz"))
  }
}