// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

class InverseImageFilter extends java.awt.image.RGBImageFilter {
  private final val OpaqueAlpha = 0xff000000
  canFilterIndexColorModel = true
  override def filterRGB(x: Int, y: Int, rgb: Int): Int = {
    var red = (rgb >> 16) & 0xff
    var green = (rgb >> 8) & 0xff
    var blue = (rgb) & 0xff
    (rgb & OpaqueAlpha) | ((255 - red) << 16) | ((255 - green) << 8) | (255 - blue)
  }
}
