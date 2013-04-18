package org.nlogo.review

import java.awt.{ Graphics, Graphics2D }

import org.nlogo.plot.Plot
import org.nlogo.window.{ InterfaceColors, PlotWidget, PlotWidgetGUI }

import javax.swing.JPanel

trait HasPlotPanels extends HasCurrentRun#Sub { this: JPanel =>

  val reviewTab: ReviewTab
  private var plotPanels: Map[String, PlotPanel] = Map()

  setLayout(null) // disable layout manager to use absolute positioning
  reviewTab.state.subscribe(this) // subscribe to current run change events

  override def notify(pub: ReviewTabState#Pub, event: CurrentRunChangeEvent) {
    event match {
      case AfterCurrentRunChangeEvent(_, newRun) =>
        plotPanels.values.foreach(remove) // remove old panels
        if (newRun.isDefined) initPlotPanels()
      case _ =>
    }
  }

  def initPlotPanels() {
    val container = reviewTab.ws.viewWidget.findWidgetContainer
    plotPanels = reviewTab.workspaceWidgets.collect {
      case plotWidget: PlotWidget =>
        val panel = new PlotPanel(
          plotWidget.plot,
          container.getUnzoomedBounds(plotWidget),
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
  val legendIsOpen: Boolean) extends JPanel {

  setBounds(bounds)
  setBorder(org.nlogo.swing.Utils.createWidgetBorder)
  setBackground(InterfaceColors.PLOT_BACKGROUND)
  val gui = new PlotWidgetGUI(initialPlot, PlotPanel.this)
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
