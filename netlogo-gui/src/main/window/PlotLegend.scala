// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, FlowLayout, Font, Graphics }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.plot.PlotPen
import org.nlogo.swing.Transparent
import org.nlogo.theme.InterfaceColors

class PlotLegend(widget: AbstractPlotWidget, boldName: Boolean)
  extends JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) with Transparent {

  var open = false

  def addPen(pen: PlotPen) {
    if (open) {
      if (pen.inLegend)
        add(new LegendItem(pen))

      revalidate()
    }
  }

  def toggle() { open = !open; refresh() }
  def refresh() { clearGUI(); if (open) fillGUI() }
  def clearGUI() { removeAll(); revalidate() }

  private def fillGUI() {
    for (pen <- widget.plot.pens; if (pen.inLegend))
      add(new LegendItem(pen))

    revalidate()
  }

  private class LegendItem(pen: PlotPen) extends JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) with Transparent {
    add(new JPanel {
      setBackground(new Color(pen.color))

      override def getPreferredSize: Dimension =
        new Dimension(widget.zoom(15), widget.zoom(2))
    })

    add(new JLabel(pen.name) {
      if (boldName)
        setFont(getFont.deriveFont(Font.BOLD))

      override def paintComponent(g: Graphics) {
        setForeground(InterfaceColors.widgetText)

        super.paintComponent(g)
      }
    })
  }
}
