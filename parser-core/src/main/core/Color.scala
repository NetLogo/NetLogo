// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import ColorConstants._

trait Color {

  // these define the structure of NetLogo's color space, namely,
  // a range of [0.0,140.0)
  val NumHues = 14
  val MaxColor = 10 * NumHues

  val BoxedBlack = 0: java.lang.Double
  val BoxedWhite = White: java.lang.Double

  // this defines how the colors actually look.  note that because of the funky way we scale the
  // ranges, these differ slightly from the actual colors that end up on screen, so remember to
  // never access this directly, only use it to fill ARGB_CACHE, since we do the scaling as we fill
  // the cache - ST 5/11/05
  private val ColorsRGB = Array[Int](
    140, 140, 140, // gray       (5)
    215, 48, 39, // red       (15)
    241, 105, 19, // orange    (25)
    156, 109, 70, // brown     (35)
    237, 237, 47, // yellow    (45)
    87, 176, 58, // green     (55)
    42, 209, 57, // lime      (65)
    27, 158, 119, // turquoise (75)
    82, 196, 196, // cyan      (85)
    43, 140, 190, // sky       (95)
    50, 92, 168, // blue     (105)
    123, 78, 163, // violet   (115)
    166, 25, 105, // magenta  (125)
    224, 126, 149, // pink     (135)
    0, 0, 0, // black
    255, 255, 255)   // white

  // keep the same information in different forms in some extra arrays, for fast access later
  private val ARGB_Cache = {
    val cache = (0 until MaxColor * 10).map(computeARGBforCache).toArray
    // override the entries for white and black to be pure white and pure black instead of gray
    cache(0) = 0xff << 24
    cache(99) = 0xffffffff
    cache
  }

  // also keep a cache of reverse lookups from rgb values to NetLogo color numbers, for the benefit
  // of import-pcolors
  protected val rgbMap = {
    val map = collection.mutable.HashMap[Double, Double]()
    for (c <- 0 until MaxColor * 10) {
      val color = c / 10.0
      map(getARGBbyPremodulatedColorNumber(color)) = color
    }
    map
  }

  // input: any
  // output: [0-139]
  def modulateInteger(color: Int): Int = {
    var c = color
    if (c < 0 || c >= MaxColor) {
      c %= MaxColor
      if (c < 0)
        c += MaxColor
    }
    c
  }

  // input: any
  // output: [0.0-140.0)
  def modulateDouble(color: java.lang.Double): Double =
    modulateDouble(color.doubleValue)

  // input: any
  // output: [0.0-140.0)
  def modulateDouble(color: Double): Double = {
    var c = color
    if (c < 0 || c >= MaxColor) {
      c %= MaxColor
      if (c < 0)
        c += MaxColor
      // we have to be careful here because extremely small negative values may equal 140 when added
      // to 140.  Gotta love floating point math...  - ST 10/20/04
      if (c >= MaxColor)
        c = 139.9999999999999
    }
    c
  }

  // input: any
  // output: 0.0 or 5.0 or 15.0 or ... or 135.0
  def findCentralColorNumber(color: Double) = { /* all shades of a color return the same color
                                                 * i.e.  blue, blue - 5, blue + 4.9999 will return
                                                 * the same thing
                                                 */
    val c =
      if (color < 0 || color >= MaxColor)
        modulateDouble(color)
      else
        color
    ((c / 10).toInt + 0.5) * 10
  }

  // given a color in ARGB, function returns a value in the range 0 - 140
  // that represents the color in NetLogo's color scheme
  // input: ARGB
  // output: [0.0-139.9]
  def getClosestColorNumberByARGB(argb: Int): Double =
    rgbMap.get(argb).getOrElse(
      estimateClosestColorNumberByRGB(argb))

  protected def estimateClosestColorNumberByRGB(argb: Int) = {
    var smallestDistance = 100000000L
    var closest = 0.0
    for((k, v) <- rgbMap) {
      val candidate = k.toInt
      val dist = colorDistance(argb, candidate)
      if (dist < smallestDistance) {
        smallestDistance = dist
        closest = v
      }
    }
    closest
  }

  // Java code translated from a C snippet at www.compuphase.com/cmetric.htm
  private def colorDistance(argb1: Int, argb2: Int): Long = {
    val r1 = argb1 >> 16 & 0xff
    val g1 = argb1 >> 8 & 0xff
    val b1 = argb1 & 0xff
    val r2 = argb2 >> 16 & 0xff
    val g2 = argb2 >> 8 & 0xff
    val b2 = argb2 & 0xff
    val rmean = r1 + r2 / 2
    val rd = r1 - r2
    val gd = g1 - g2
    val bd = b1 - b2
    (((512 + rmean) * rd * rd) >> 8) + 4 * gd * gd + (((767 - rmean) * bd * bd) >> 8)
  }

  // input: [0.0-140.0)
  // output: ARGB
  def getARGBbyPremodulatedColorNumber(modulatedColor: Double): Int =
    // note that we're rounding down to the nearest 0.1 - ST 5/30/05
    ARGB_Cache((modulatedColor * 10).toInt)

  // Used only for filling the ARGB_CACHE array.  This is the method that determines how color
  // numbers that don't end in 5.0 actually wind up looking on the screen, by adjusting the
  // saturation or brightness according to distance from 5.0.  Below 5.0 we decrease brightness;
  // above 5.0 we decrease saturation.  (Actually we're working in RGB space not HSB so we just
  // increase or decrease the RGB values.)
  // "Premodulated" means we assume the input is already in the [0.0-140) range.
  private def computeARGBforCache(colorTimesTen: Int): Int = {
    val baseIndex = colorTimesTen / 100
    var r = ColorsRGB(baseIndex * 3)
    var g = ColorsRGB(baseIndex * 3 + 1)
    var b = ColorsRGB(baseIndex * 3 + 2)
    // this is sneaky... we want the range of colors we are mapping to get VERY VERY close to black
    // at one end and white at the other, but we don't want to get all the way to actual black or
    // white, because then color-under wouldn't be able to do the reverse mapping back to the
    // original color number.  so instead of dividing by 50, we divide by a slightly larger number;
    // that gives us a slightly narrower range.  then we need to move the numbers up a bit to get
    // black away from 0.0 without causing the whites (9.9, 19.9, 29.9) to hit pure white.  the
    // actual numbers 50.48 and 0.012 were arrived at by trial and error and might not achieve the
    // absolute widest possible spread, I don't know, but they seem good enough. - ST 4/30/05
    val step = ((colorTimesTen % 100 - 50)) / 50.48 + 0.012
    if (step < 0.0) {
      r += (r * step).toInt
      g += (g * step).toInt
      b += (b * step).toInt
    } else if (step > 0.0) {
      r += ((0xff - r) * step).toInt
      g += ((0xff - g) * step).toInt
      b += ((0xff - b) * step).toInt
    }
    (0xff << 24) + (r << 16) + (g << 8) + b
  }

  ///

  // input: color name in lowercase
  // output: ARGB
  def getRGBByName(name: String): Int =
    getARGBByIndex(ColorNames.indexOf(name))

  // input: [0-15]
  // output: ARGB
  def getARGBByIndex(index: Int): Int =
    index match {
      case 14 => // black
        0xff000000
      case 15 => // white
        0xffffffff
      case _ =>
        ARGB_Cache(index * 100 + 50)
    }

  def getARGBIntByRGBAList(rgba: LogoList): Int =
    if (rgba.size == 4)
      (((rgba.get(3).asInstanceOf[java.lang.Double]).intValue << 24) |
       ((rgba.get(0).asInstanceOf[java.lang.Double]).intValue << 16) |
       ((rgba.get(1).asInstanceOf[java.lang.Double]).intValue << 8) |
       ((rgba.get(2).asInstanceOf[java.lang.Double]).intValue))
    else
      (255 << 24 |
       ((rgba.get(0).asInstanceOf[java.lang.Double]).intValue << 16) |
       ((rgba.get(1).asInstanceOf[java.lang.Double]).intValue << 8) |
       ((rgba.get(2).asInstanceOf[java.lang.Double]).intValue))

  def getARGBIntByRgbColor(c: RgbColor): Int =
    (c.alpha << 24 |
     c.red   << 16 |
     c.green << 8  |
     c.blue)

  // this assumes that you have an RGB color that is actually one of the NetLogo colors
  def argbToColor(argb: Int): java.lang.Double =
    getClosestColorNumberByARGB(argb)

  def getRGBInt(r: Int, g: Int, b: Int): Int =
    (((r << 8) + g) << 8) + b

  def getRGBInt(c: AnyRef): Int =
    c match {
      case l: LogoList =>
        getARGBIntByRGBAList(l)
      case d: java.lang.Double =>
        getARGBbyPremodulatedColorNumber(d.doubleValue)
      case _ =>
        throw new IllegalArgumentException(
          s"Can't get RGB color: $c")
    }
}

object Color extends Color
