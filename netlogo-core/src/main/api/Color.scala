// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.text.DecimalFormat
import java.awt.{ Color => JColor }

import org.nlogo.core.{ Color => CColor, ColorConstants, LogoList, I18N, Resource },
  ColorConstants._

object Color extends CColor {

  // these define NetLogo's color names and how they map into the [0.0,140.0) range
  val ColorNames = Array(
    // the 13 base hues
    "gray", "red", "orange", "brown",
    "yellow", "green", "lime", "turquoise", "cyan", "sky",
    "blue", "violet", "magenta", "pink",
    // plus two special cases
    "black", "white")

  // input: [0-15]
  def getColorNameByIndex(index: Int): String =
    ColorNames(index)

  private val AWT_Cache =
    for(i <- (0 until MaxColor * 10).toArray)
    yield new JColor(
      getARGBbyPremodulatedColorNumber(i / 10.0))

  private val ColorNumbers = Array[Double](
    // the 13 base hues
    5.0, 15.0, 25.0, 35.0, 45.0, 55.0, 65.0,
    75.0, 85.0, 95.0, 105.0, 115.0, 125.0, 135.0,
    // plus two special cases
    Black, White)

  def getColorNumberByIndex(index: Int): Double =
    ColorNumbers(index)

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
    // restrict to HSB range
    val hh = 0f max h min 360.0f
    val ss = 0f max s min 100.0f
    val bb = 0f max b min 100.0f
    // convert to RGB
    val argb = JColor.HSBtoRGB(hh / 360.0f, ss / 100.0f, bb / 100.0f)
    rgbMap.get(argb).getOrElse(
      // try the new search mechanism
      estimateClosestColorNumberByRGB(argb))
  }

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
            (hsb(0) * 360.0f, 3)))
    result.add(Double.box
        (org.nlogo.api.Approximate.approximate
            (hsb(1) * 100.0f, 3)))
    result.add(Double.box
        (org.nlogo.api.Approximate.approximate
            (hsb(2) * 100.0f, 3)))
    result.toLogoList
  }

  def getHSBListByRGBList(rgbList: LogoList): LogoList = {
    val r = rgbList.get(0).asInstanceOf[Double].intValue
    val g = rgbList.get(1).asInstanceOf[Double].intValue
    val b = rgbList.get(2).asInstanceOf[Double].intValue
    val hsbvals = java.awt.Color.RGBtoHSB(
          (StrictMath.max(0, StrictMath.min(255, r))),
          (StrictMath.max(0, StrictMath.min(255, g))),
          (StrictMath.max(0, StrictMath.min(255, b))),
          null)

    val llb = new LogoListBuilder
    llb.add(Double.box(org.nlogo.api.Approximate.approximate(hsbvals(0) * 360.0f, 3)))
    llb.add(Double.box(org.nlogo.api.Approximate.approximate(hsbvals(1) * 100.0f, 3)))
    llb.add(Double.box(org.nlogo.api.Approximate.approximate(hsbvals(2) * 100.0f, 3)))
    llb.toLogoList
  }

  // the name should tip you off - do not use this function.
  // takes a perfectly good HSB list and converts it to the old (wrong) format.
  // RG 2/16
  def convertGoodHSBListToDumbOldHSBFormat(hsbvals: LogoList): LogoList = {
    val hsbList = new LogoListBuilder
    hsbList.add(Double.box(org.nlogo.api.Approximate.approximate((255.0f / 360.0f) * hsbvals(0).asInstanceOf[java.lang.Double], 3)))
    hsbList.add(Double.box(org.nlogo.api.Approximate.approximate((255.0f / 100.0f) * hsbvals(1).asInstanceOf[java.lang.Double], 3)))
    hsbList.add(Double.box(org.nlogo.api.Approximate.approximate((255.0f / 100.0f) * hsbvals(2).asInstanceOf[java.lang.Double], 3)))
    hsbList.toLogoList
  }

  def getHSBListByColor(color: Double): LogoList = {
    val ccolor = if (color < 0 || color >= 140) modulateDouble(color) else color
    getHSBListByARGB(getARGBbyPremodulatedColorNumber(ccolor))
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


  private val ColorTranslations = "/system/color-translation.txt";
  private lazy val colorTranslations = {
    val map = collection.mutable.HashMap[Double, Int]()
    val lines = Resource.get(ColorTranslations).getLines
    for(line <- lines.map(_.trim).filter(_.nonEmpty).filter(_.head != '#')) {
      val strs = line.split("\\s+")
      val index = strs.head.toInt
      map ++= (1 until strs.size).map(j => (strs(j).toDouble, index))
    }
    map
  }

  // this handles translation from pre-3.0 color palette to new palette
  // input: ARGB
  // output: ARGB
  def translateSavedColor(color: Int): Int =
    colorTranslations
      .get(color.toDouble)
      .map(_.intValue)
      .map(org.nlogo.api.Color.getARGBByIndex)
      .getOrElse(color)
}
