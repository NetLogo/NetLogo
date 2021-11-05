// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render

import org.scalatest.funsuite.AnyFunSuite

class RendererTests extends AnyFunSuite {
  // If we're not using the right renderer on Mac OS X, then a bunch of other tests
  // will fail (e.g. TestChecksums since the drawing checksums change depending on
  // the renderer). - ST 10/22/09
  test("Sun renderer") {
    assertResult("false")(System.getProperty("apple.awt.graphics.UseQuartz"))
  }
}
