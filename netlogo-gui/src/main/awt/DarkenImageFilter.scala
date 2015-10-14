// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

class DarkenImageFilter(level: Double)
extends java.awt.image.RGBImageFilter {
  private final val OpaqueAlpha = 0xff000000
  canFilterIndexColorModel = true
  override def filterRGB(x: Int, y: Int, rgb: Int): Int = {
    var red = (rgb >> 16) & 0xff
    var green = (rgb >> 8) & 0xff
    var blue = (rgb) & 0xff
    red = ((1.0 - level) * red).toInt
    green = ((1.0 - level) * green).toInt
    blue = ((1.0 - level) * blue).toInt
    (rgb & OpaqueAlpha) | (red << 16) | (green << 8) | blue
  }
}
