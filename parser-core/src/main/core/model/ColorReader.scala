// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import
  java.lang.{ Integer => JInteger }

import
  cats.data.Validated,
    Validated.{ Invalid, Valid }

import
  org.nlogo.core.{ Color, RgbColor }

import
  org.nlogo.xmllib.{ Element, InvalidAttribute, ParseError, XmlReader }

object ColorReader {
  def reader(name: String): XmlReader[Element, RgbColor] =
    new XmlReader.AttributeReader(name).flatMap(readHexToRgbColor(name))

  def doubleReader(name: String): XmlReader[Element, Double] =
    reader(name).map(rgbColorToDouble _)

  def rgbColorToHex(c: RgbColor): String = {
    val i = Color.getARGBIntByRgbColor(c) & 0xffffff // strip off alpha channel
    val baseHexString = Integer.toHexString(i)
    val leadingZeros = 6 - baseHexString.length
    s"#${"0" * leadingZeros}${baseHexString}".toUpperCase
  }

  def colorDoubleToHex(d: Double): String =
    rgbColorToHex(doubleToRgbColor(d))

  private def readHexToRgbColor(keyName: String)(hexString: String): Validated[ParseError, RgbColor] = {
    if (hexString.length < 7)
      Invalid(InvalidAttribute(Seq(), keyName, hexString))
    else {
      try {
        Valid(hexColorToRgbColor(hexString))
      } catch {
        case e: NumberFormatException =>
          Invalid(InvalidAttribute(Seq(), keyName, hexString))
      }
    }
  }

  def hexColorToRgbColor(hexString: String): RgbColor = {
    val (rs, gs, bs) = (hexString.substring(1, 3), hexString.substring(3, 5), hexString.substring(5, 7))
    val r = JInteger.valueOf(rs, 16)
    val g = JInteger.valueOf(gs, 16)
    val b = JInteger.valueOf(bs, 16)
    RgbColor(r, g, b)
  }

  def rgbColorToDouble(color: RgbColor): Double =
    Color.getClosestColorNumberByARGB(Color.getRGBInt(color.red, color.green, color.blue))

  def doubleToRgbColor(d: Double): RgbColor = {
    val i = Color.getARGBbyPremodulatedColorNumber(d) & 0xffffffff
    RgbColor((i >> 16) & 0xff, (i >> 8) & 0xff, i & 0xff, (i >> 24) & 0xff)
  }
}
