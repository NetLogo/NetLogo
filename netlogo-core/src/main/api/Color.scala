// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.text.DecimalFormat
import java.awt.{ Color => JColor }

import org.nlogo.core.{ Color => CColor, ColorConstants, LogoList, I18N },
  ColorConstants._

object Color extends CColor {
  private val AWT_Cache =
    for(i <- (0 until MaxColor * 10).toArray)
    yield new JColor(
      getARGBbyPremodulatedColorNumber(i / 10.0))

  val BaseColors = LogoList(
    (0 to 13).map(n => Double.box(n * 10 + 5)): _*)

  def getColor(color: AnyRef): JColor = {
    color match {
      case d: java.lang.Double =>
        AWT_Cache((d.doubleValue * 10).toInt)
      case list: LogoList if list.size == 3 =>
        new JColor(list.get(0).asInstanceOf[Number].intValue,
                           list.get(1).asInstanceOf[Number].intValue,
                           list.get(2).asInstanceOf[Number].intValue)
      case list: LogoList if list.size == 4 =>
        new JColor(list.get(0).asInstanceOf[Number].intValue,
                           list.get(1).asInstanceOf[Number].intValue,
                           list.get(2).asInstanceOf[Number].intValue,
                           list.get(3).asInstanceOf[Number].intValue)
    }
  }

  // given a color in ARGB, function returns a string in the "range" of
  // "red - 5" to "magenta + 5" representing the color in NetLogo's color scheme
  // input: ARGB
  // output: ["red - 5" to "magenta + 5"]
  def getClosestColorNameByARGB(argb: Int): String = {
    val formatter = new DecimalFormat("###.####")
    getClosestColorNumberByARGB(argb) match {
      case Black =>
        getColorNameByIndex(14)
      case White =>
        getColorNameByIndex(15)
      case closest =>
        val baseColorNumber = findCentralColorNumber(closest).toInt
        val difference = closest - baseColorNumber
        val baseColorName = getColorNameByIndex((baseColorNumber - 5) / 10)
        if (difference == 0)
          baseColorName
        else if (difference > 0)
          baseColorName + " + " + formatter.format(StrictMath.abs(difference))
        else
          baseColorName + " - " + formatter.format(StrictMath.abs(difference))
    }
  }

  // given a color in the HSB spectrum, function returns a value
  // that represents the color in NetLogo's color scheme
  // inputs: clamped to [0.0-1.0]
  // output: [0.0-139.9]
  def getClosestColorNumberByHSB(h: Float, s: Float, b: Float) = {
    // restrict to 0-255 range
    val hh = 0f max h min 255f
    val ss = 0f max s min 255f
    val bb = 0f max b min 255f
    // convert to RGB
    val argb = JColor.HSBtoRGB(hh / 255, ss / 255, bb / 255)
    rgbMap.get(argb).getOrElse(
      // try the new search mechanism
      estimateClosestColorNumberByRGB(argb))
  }

  ///

  def getRGBListByARGB(argb: Int): LogoList = {
    val result = new LogoListBuilder
    // 3 is just enough digits of precision so that passing the resulting values through the rgb
    // prim will reconstruct the original number (or rather the floor of the original number to the
    // nearest 0.1) - ST 10/25/05
    result.add(org.nlogo.api.Approximate.approximate(((argb >> 16) & 0xff), 3): java.lang.Double)
    result.add(org.nlogo.api.Approximate.approximate(((argb >> 8) & 0xff), 3): java.lang.Double)
    result.add(org.nlogo.api.Approximate.approximate(((argb) & 0xff), 3): java.lang.Double)
    result.toLogoList
  }

  def getRGBAListByARGB(argb: Int): LogoList = {
    val result = new LogoListBuilder
    // 3 is just enough digits of precision so that passing the resulting values through the rgb
    // prim will reconstruct the original number (or rather the floor of the original number to the
    // nearest 0.1) - ST 10/25/05
    result.add(
      Double.box(
        org.nlogo.api.Approximate.approximate(((argb >> 16) & 0xff), 3)))
    result.add(
      Double.box(
        org.nlogo.api.Approximate.approximate(((argb >> 8) & 0xff), 3)))
    result.add(Double.box(
      org.nlogo.api.Approximate.approximate(((argb) & 0xff), 3)))
    result.add(Double.box(
      org.nlogo.api.Approximate.approximate(((argb >> 24) & 0xff), 3)))
    result.toLogoList
  }

  def getHSBListByARGB(argb: Int): LogoList = {
    val hsb = new Array[Float](3)
    JColor.RGBtoHSB(
      (argb >> 16) & 0xff,
      (argb >> 8) & 0xff,
      (argb) & 0xff,
      hsb)
    val result = new LogoListBuilder
    // 3 is just enough digits of precision so that passing the resulting values through the hsb
    // prim will reconstruct the original number (or rather the floor of the original number to the
    // nearest 0.1) - ST 10/25/05
    result.add(Double.box
        (org.nlogo.api.Approximate.approximate
            (hsb(0) * 255, 3)))
    result.add(Double.box
        (org.nlogo.api.Approximate.approximate
            (hsb(1) * 255, 3)))
    result.add(Double.box
        (org.nlogo.api.Approximate.approximate
            (hsb(2) * 255, 3)))
    result.toLogoList
  }

  def getComplement(color: JColor): JColor = {
    val rgb = color.getRGBColorComponents(null)
    new JColor(
      (rgb(0) + 0.5f) % 1.0f,
      (rgb(1) + 0.5f) % 1.0f,
      (rgb(2) + 0.5f) % 1.0f)
  }

  @throws(classOf[AgentException])
  def validRGBList(rgb: LogoList, allowAlpha: Boolean) {
    def validRGB(c: Int) {
      if (c < 0 || c > 255)
        throw new AgentException(I18N.errors.get(
          "org.nlogo.agent.Agent.rgbValueError"))
    }
    if (rgb.size == 3 || (allowAlpha && rgb.size == 4))
      try {
        var i = 0
        while (i < rgb.size) {
          validRGB(rgb.get(i).asInstanceOf[java.lang.Double].intValue)
          i += 1
        }
        return
      }
      catch { case e: ClassCastException =>
        // just fall through and throw the error below
        org.nlogo.api.Exceptions.ignore(e)
      }
    val key = "org.nlogo.agent.Agent." +
      (if (allowAlpha) "rgbListSizeError.3or4"
       else "rgbListSizeError.3")
    throw new AgentException(I18N.errors.get(key))
  }

}
