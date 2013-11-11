// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import org.nlogo.mirror.ModelRun
import org.nlogo.plot.Plot
import org.nlogo.window.InterfaceColors
import org.nlogo.window.PlotWidgetGUI

import javax.swing.JPanel

class PlotPanel(
  run: ModelRun,
  font: Font,
  initialPlot: Plot,
  bounds: java.awt.Rectangle,
  legendIsOpen: Boolean) extends JPanel {
  setFont(font)
  setBounds(bounds)
  setBorder(org.nlogo.swing.Utils.createWidgetBorder)
  setBackground(InterfaceColors.PLOT_BACKGROUND)
  val gui = new PlotWidgetGUI(initialPlot, this)
  gui.legend.open = legendIsOpen
  gui.addToPanel(this)

  override def paintComponent(g: Graphics): Unit =
    for {
      frame <- run.currentFrame
      plot <- frame.plots.find(_.name == initialPlot.name)
    } {
      g.asInstanceOf[Graphics2D].setRenderingHint(
        KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
      gui.plot = plot
      super.paintComponent(g)
    }
}
