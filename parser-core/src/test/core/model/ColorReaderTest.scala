// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.scalatest.FunSuite

import org.nlogo.core.RgbColor

class ColorReaderTest extends FunSuite {
  test("converts doubles to appropriate RgbColors") {
    assertResult(RgbColor(239, 173, 169))(ColorReader.doubleToRgbColor(18.0))
    assertResult(RgbColor(0, 0, 0))(ColorReader.doubleToRgbColor(0.0))
  }

  def doubleToHex(d: Double): String =
    ColorReader.rgbColorToHex(ColorReader.doubleToRgbColor(d))

  def hexToDouble(hex: String): Double =
    ColorReader.rgbColorToDouble(ColorReader.hexColorToRgbColor(hex))

  def convertsBack(hex: String): Unit =
    test(s"converts ${hex} to double and back") {
      assertResult(hex, s"Expected ${hex} to be converted to a double and back")(doubleToHex(hexToDouble(hex)))
    }

  def convertsBack(d: Double): Unit =
    test(s"converts ${d} to hex and back") {
      assertResult(d, s"Expected ${d} to be converted to hex and back")(hexToDouble(doubleToHex(d)))
    }

  convertsBack(18.0)
  convertsBack("#000000")
  convertsBack("#A51969")
  convertsBack(124.9)

}
