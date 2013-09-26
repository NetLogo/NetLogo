// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics
import java.awt.Graphics2D

import org.nlogo.plot.Plot
import org.nlogo.window.InterfaceColors
import org.nlogo.window.PlotWidget
import org.nlogo.window.PlotWidgetGUI

import javax.swing.JPanel

trait HasPlotPanels { this: JPanel =>

  val reviewTab: ReviewTab
  private var plotPanels: Map[String, PlotPanel] = Map()

  setLayout(null) // disable layout manager to use absolute positioning

  reviewTab.state.afterRunChangePub.newSubscriber { event =>
    plotPanels.values.foreach(remove) // remove old panels
    if (event.newRun.isDefined) initPlotPanels()
  }

  def initPlotPanels() {
    val container = reviewTab.ws.viewWidget.findWidgetContainer
    plotPanels = reviewTab.workspaceWidgets.collect {
      case plotWidget: PlotWidget =>
        val panel = new PlotPanel(
          plotWidget.plot,
          container.getUnzoomedBounds(plotWidget),
          plotWidget,
          plotWidget.gui.legend.open)
        add(panel)
        plotWidget.plot.name -> panel
    }(scala.collection.breakOut)
  }

  def refreshPlotPanels() {
    for {
      frame <- reviewTab.state.currentFrame
      plot <- frame.plots
      panel <- plotPanels.get(plot.name)
    } {
      panel.refresh(plot)
    }
  }
}

class PlotPanel(
  initialPlot: Plot,
  bounds: java.awt.Rectangle,
  fontSource: java.awt.Component,
  val legendIsOpen: Boolean) extends JPanel {

  setBounds(bounds)
  setBorder(org.nlogo.swing.Utils.createWidgetBorder)
  setBackground(InterfaceColors.PLOT_BACKGROUND)
  val gui = new PlotWidgetGUI(initialPlot, fontSource)
  gui.legend.open = legendIsOpen
  gui.addToPanel(PlotPanel.this)

  def refresh(plot: Plot) {
    gui.plot = plot
    gui.refreshAxisLabels()
  }

  override def paintComponent(g: Graphics): Unit = {
    g.asInstanceOf[Graphics2D].setRenderingHint(
      java.awt.RenderingHints.KEY_ANTIALIASING,
      java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
    super.paintComponent(g)
  }
}
