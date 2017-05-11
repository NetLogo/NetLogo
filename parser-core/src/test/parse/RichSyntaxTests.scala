// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.Syntax
import org.scalatest.FunSuite

class RichSyntaxTests {
  // The syntax object is both extremely complicated in terms of data and quite opaque in terms of API.
  // The purpose of this wrapper class is to simplify the operation without needing to modify it in org.nlogo.core.
  // Eventually, we might hope to replace the implementation in org.nlogo.core with something like this, which
  // captures requirements with a more succinct API.

  test("basic command syntax") {
    val syntax = RichSyntax(Syntax.command(), Seq())
    assertResult(None)(syntax.nextArgumentType)
  }


}
