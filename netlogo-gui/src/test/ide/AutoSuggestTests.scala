// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import org.nlogo.core.NetLogoCore
import org.nlogo.util.AnyFunSuiteEx

class AutoSuggestTests extends AnyFunSuiteEx {
  private val autoSuggest = AutoSuggest(NetLogoCore, None)

  test("invalid string") {
    assertResult(Seq())(autoSuggest.getSuggestions("zzzzz"))
  }

  test("partially completed unique command") {
    assertResult(Seq("print"))(autoSuggest.getSuggestions("pri"))
  }

  test("completed unique command") {
    assertResult(Seq("print"))(autoSuggest.getSuggestions("print"))
  }

  test("completed prefix command") {
    assertResult(Seq("ask", "ask-concurrent"))(autoSuggest.getSuggestions("ask"))
  }

  test("valid suffix") {
    assertResult(Seq())(autoSuggest.getSuggestions("concurrent"))
  }

  test("empty string") {
    val allTokens = (NetLogoCore.tokenMapper.allCommandNames ++ NetLogoCore.tokenMapper.allReporterNames)
                      .toSeq.map(_.toLowerCase).sorted

    assertResult(allTokens)(autoSuggest.getSuggestions(""))
  }
}
