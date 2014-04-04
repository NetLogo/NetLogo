// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.front

// We have this because in the future we might change how primitives specify their syntax, or how
// the syntaxes are stored.  Having all of the syntaxes here in a simple textual format guards
// against regressions.  (It also means new primitives must be added to resources/test/syntaxes.txt
// at the same time they are added to tokens.txt.) - ST 12/5/09, 4/4/14

import org.scalatest.FunSuite
import org.nlogo.core.Syntaxes
import org.nlogo.api.Resource

class TestAllSyntaxes extends FunSuite {
  def shorten(name: String) =
    name.reverse.takeWhile(_ != '.').reverse
  def entry(name: String) =
    shorten(name) + " " + Syntaxes.syntaxes(shorten(name)).dump
  test("syntaxes match") {
    assertResult(Resource.getResourceLines("/syntaxes.txt").toSeq)(
      FrontEnd.tokenMapper.allClassNames.map(entry).toSeq.sorted)
  }
}
