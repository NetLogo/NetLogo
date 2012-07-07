// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render

import org.scalatest.FunSuite

class RendererTests extends FunSuite {
  // If we're not using the right renderer on Mac OS X, then a bunch of other tests
  // will fail (e.g. TestChecksums since the drawing checksums change depending on
  // the renderer). - ST 10/22/09
  test("Sun renderer") {
    expect("false")(System.getProperty("apple.awt.graphics.UseQuartz"))
  }
}
