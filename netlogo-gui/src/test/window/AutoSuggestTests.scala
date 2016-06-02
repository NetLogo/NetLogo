// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.scalatest.FunSuite

class AutoSuggestTests extends FunSuite {
  test("empty"){
    import AutoSuggest.getSuggestion
    val testList = List("zzz")
    assertResult(testList)(getSuggestions("zzz"))
  }
}