// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.Color

object Colors {

  /**
   * Mixes the rgb components of two colors.
   *
   * @param mix the proportion, from 0 to 1, of the first color in the mix.
   * @return a new color with <code>red = mix*(c1.red) + (1-mix)*c2.red</code>, etc.
   */
  def mixColors(c1: Color, c2: Color, mix: Double): Color = {
    require(mix >= 0.0 && mix <= 1.0)
    new Color(((c1.getRed * mix) + (c2.getRed * (1 - mix))).toInt,
              ((c1.getGreen * mix) + (c2.getGreen * (1 - mix))).toInt,
              ((c1.getBlue * mix) + (c2.getBlue * (1 - mix))).toInt)
  }

  /**
   * Converts a java.awt.Color to a 6-digit hex string suitible for HTML/CSS tags. *
   */
  def AWTColorToHex(c: Color) =
    java.lang.Integer.toHexString(c.getRGB).takeRight(6)

  /**
   * Wraps a string with HTML font tag for color. *
   */
  def colorize(s: String, c: Color) =
    "<font color=\"#" + AWTColorToHex(c) + "\">" + s + "</font>"

}
