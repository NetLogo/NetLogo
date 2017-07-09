// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import org.scalatest.FunSuite

class AutoSuggestTests extends FunSuite {
  val autoSuggest = new AutoSuggest(Set.empty[String], () => Set.empty[String])
  test("empty"){
    val testList = Seq()
    assertResult(testList)(autoSuggest.getSuggestions("zzzzz"))
  }
}
