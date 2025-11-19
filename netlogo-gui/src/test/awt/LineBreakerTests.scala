// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.Font
import java.awt.image.BufferedImage

import org.nlogo.util.AnyFunSuiteEx

class LineBreakerTests extends AnyFunSuiteEx {
  test("don't blow up") {
    val image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)  // arbitrary type
    val metrics = image.getGraphics.getFontMetrics(new Font("courier", Font.PLAIN, 400))
    // We're not testing that we get reasonable answers, only that it doesn't blow up or hang
    LineBreaker.breakLines(
      "this is a test", metrics, 10)
    LineBreaker.breakLines(
      "thisisatestthisisatestthisisatestthisisatestthis" +
      "isatestthisisatestthisisatestthisisatestthisisat" +
      "estthisisatestthisisatestthisisatestthisisatestt" +
      "hisisatestthisisatestthisisatest",
      metrics, 10)
  }
}
