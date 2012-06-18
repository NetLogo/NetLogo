// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.plot.{ Plot, PlotPen }

// we use fontSource in order to keep the font sizes of the PlotPen objects
// in sync with the current zoom level even when the PlotLegend is
// closed.  a bit kludgy perhaps, but oh well - ST 9/2/04
class PlotLegend(plot: Plot, fontSource: java.awt.Component)
extends javax.swing.JPanel {

  setLayout(new org.nlogo.awt.ColumnLayout(2)) // 2 pixel gap
  setOpaque(false)

  var open = false

  def addPen(pen: PlotPen) {
    if (open) {
      if (pen.inLegend) {
        add(new LegendItem(pen) { setFont(fontSource.getFont) })
      }
      revalidate()
    }
  }

  def toggle() { open = !open; refresh() }
  def refresh() { clearGUI(); if (open) fillGUI() }
  def clearGUI() { removeAll(); revalidate() }

  private def fillGUI() {
    for (pen <- plot.pens; if (pen.inLegend)) {
      add(new LegendItem(pen) { setFont(fontSource.getFont) })
    }
    revalidate()
  }

  private class LegendItem(pen: PlotPen) extends javax.swing.JComponent {
    org.nlogo.awt.Fonts.adjustDefaultFont(this)

    override def paintComponent(g: java.awt.Graphics) {
      val ascent = g.getFontMetrics.getMaxAscent
      g.setColor(new java.awt.Color(pen.color))
      g.fillRect(0, 0, ascent, ascent)
      g.setColor(getForeground)
      g.drawRect(0, 0, ascent, ascent)
      g.drawString(pen.name, ascent + 4, ascent)
    }

    override def getPreferredSize = getMinimumSize
    override def getMinimumSize = {
      val metrics = getFontMetrics(getFont())
      new java.awt.Dimension(metrics.stringWidth(pen.name) + metrics.getMaxAscent + 4,
        StrictMath.max(8, metrics.getMaxDescent + metrics.getMaxAscent))
    }
  }

}
