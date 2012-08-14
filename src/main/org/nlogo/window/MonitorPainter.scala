// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

object MonitorPainter {

  val LEFT_MARGIN = 5
  val RIGHT_MARGIN = 6
  val BOTTOM_MARGIN = 6
  val INSIDE_BOTTOM_MARGIN = 3

  def paint(g: java.awt.Graphics,
            d: java.awt.Dimension,
            c: java.awt.Color,
            displayName: String,
            valueString: String) {
    val fm = g.getFontMetrics
    val labelHeight = fm.getMaxDescent + fm.getMaxAscent
    g.setColor(c)
    val boxHeight = StrictMath.ceil(labelHeight * 1.4).toInt
    g.drawString(
      displayName, LEFT_MARGIN,
      d.height - BOTTOM_MARGIN - boxHeight - fm.getMaxDescent - 1)
    g.setColor(java.awt.Color.WHITE)
    g.fillRect(
      LEFT_MARGIN, d.height - BOTTOM_MARGIN - boxHeight,
      d.width - LEFT_MARGIN - RIGHT_MARGIN - 1, boxHeight)
    g.setColor(java.awt.Color.BLACK)
    if (valueString.nonEmpty)
      g.drawString(
        valueString, LEFT_MARGIN + 5,
        d.height - BOTTOM_MARGIN - INSIDE_BOTTOM_MARGIN - fm.getMaxDescent)
  }

}
