// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import org.scalatest.FunSuite
import java.awt.image.BufferedImage
import java.awt.Font

class LineBreakerTests extends FunSuite {
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
