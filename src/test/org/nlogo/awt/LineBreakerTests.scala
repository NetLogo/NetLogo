package org.nlogo.awt

import org.scalatest.FunSuite

class LineBreakerTests extends FunSuite {
  test("break lines 1") {
    val image = new java.awt.image.BufferedImage(1,1,java.awt.image.BufferedImage.TYPE_INT_RGB)  // arbitrary type
    val metrics = image.getGraphics.getFontMetrics(new java.awt.Font("courier",java.awt.Font.PLAIN,400))
    // We're not testing that we get reasonable answers, only that it doesn't blow up or hang
    LineBreaker.breakLines(
      "this is a test",metrics,10)
    LineBreaker.breakLines(
      "thisisatestthisisatestthisisatestthisisatestthis" +
      "isatestthisisatestthisisatestthisisatestthisisat" +
      "estthisisatestthisisatestthisisatestthisisatestt" +
      "hisisatestthisisatestthisisatest",
      metrics,10)
  }
}
