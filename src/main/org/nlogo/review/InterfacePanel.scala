// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Color.{ GRAY, WHITE }
import org.nlogo.mirror.FakeWorld
import org.nlogo.plot.PlotPainter
import org.nlogo.window
import org.nlogo.window.PlotWidget
import javax.swing.JPanel
import org.nlogo.window.PlotWidgetGUI
import org.nlogo.window.InterfaceColors
import java.awt.Graphics
import java.awt.Graphics2D
import org.nlogo.plot.Plot

class ReviewTabPlotPanel(
  initialPlot: Plot,
  bounds: java.awt.Rectangle,
  val legendIsOpen: Boolean) extends JPanel {

  setBounds(bounds)
  setBorder(org.nlogo.swing.Utils.createWidgetBorder)
  setBackground(InterfaceColors.PLOT_BACKGROUND)
  val gui = new PlotWidgetGUI(initialPlot, this)
  gui.legend.open = legendIsOpen
  gui.addToPanel(this)

  def refresh(plot: Plot) {
    gui.plot = plot
    gui.refreshAxisLabels()
  }

  override def paintComponent(g: Graphics): Unit = {
    var g2d: Graphics2D = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(
      java.awt.RenderingHints.KEY_ANTIALIASING,
      java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
    super.paintComponent(g)
  }
}

class InterfacePanel(reviewTab: ReviewTab)
  extends JPanel
  with HasCurrentRun#Sub {

  private var plotPanels: Map[String, ReviewTabPlotPanel] = Map()

  setLayout(null) // disable layout manager to use absolute positioning
  reviewTab.state.subscribe(this) // subscribe to current run change events

  override def notify(pub: ReviewTabState#Pub, event: CurrentRunChangeEvent) {
    event match {
      case AfterCurrentRunChangeEvent(_, _) =>
        initPlotPanels()
      case _ =>
    }
  }

  def initPlotPanels() {
    val container = reviewTab.ws.viewWidget.findWidgetContainer
    plotPanels = reviewTab.workspaceWidgets.collect {
      case plotWidget: PlotWidget =>
        val panel = new ReviewTabPlotPanel(
          plotWidget.plot,
          container.getUnzoomedBounds(plotWidget),
          plotWidget.gui.legend.open)
        add(panel)
        plotWidget.plot.name -> panel
    }(scala.collection.breakOut)
  }

  def repaintView(g: java.awt.Graphics, viewArea: java.awt.geom.Area) {
    for {
      run <- reviewTab.state.currentRun
      frame <- run.currentFrame
      fakeWorld = new FakeWorld(frame.mirroredState)
      paintArea = new java.awt.geom.Area(getBounds())
      viewSettings = run.fixedViewSettings
      g2d = g.create.asInstanceOf[java.awt.Graphics2D]
    } {
      paintArea.intersect(viewArea) // avoid spilling outside interface panel
      try {
        g2d.setClip(paintArea)
        g2d.translate(viewArea.getBounds.x, viewArea.getBounds.y)
        val renderer = fakeWorld.newRenderer
        renderer.trailDrawer.readImage(frame.drawingImage)
        renderer.paint(g2d, viewSettings)
      } finally {
        g2d.dispose()
      }
    }
  }

  def repaintWidgets(g: java.awt.Graphics) {
    for {
      frame <- reviewTab.state.currentFrame
      values = frame.mirroredState
        .filterKeys(_.kind == org.nlogo.mirror.Mirrorables.WidgetValue)
        .toSeq
        .sortBy { case (agentKey, vars) => agentKey.id } // should be z-order
        .map { case (agentKey, vars) => vars(0).asInstanceOf[String] }
      (w, v) <- reviewTab.widgetHooks.map(_.widget) zip values
    } {
      val g2d = g.create.asInstanceOf[java.awt.Graphics2D]
      try {
        val container = reviewTab.ws.viewWidget.findWidgetContainer
        val bounds = container.getUnzoomedBounds(w)
        g2d.setRenderingHint(
          java.awt.RenderingHints.KEY_ANTIALIASING,
          java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setFont(w.getFont)
        g2d.clipRect(bounds.x, bounds.y, w.getSize().width, w.getSize().height) // make sure text doesn't overflow
        g2d.translate(bounds.x, bounds.y)
        w match {
          case m: window.MonitorWidget =>
            window.MonitorPainter.paint(
              g2d, m.getSize, m.getForeground, m.displayName, v)
          case _ => // ignore for now
        }
      } finally {
        g2d.dispose()
      }
    }
  }

  def repaintPlots(g: java.awt.Graphics) {
    for {
      frame <- reviewTab.state.currentFrame
      plot <- frame.plots
      panel <- plotPanels.get(plot.name)
    } {
      panel.refresh(plot)
    }
  }

  override def paintComponent(g: java.awt.Graphics) {
    super.paintComponent(g)
    g.setColor(if (reviewTab.state.currentRun.isDefined) WHITE else GRAY)
    g.fillRect(0, 0, getWidth, getHeight)
    for {
      run <- reviewTab.state.currentRun
    } {
      g.drawImage(run.interfaceImage, 0, 0, null)
      repaintView(g, run.viewArea)
      repaintWidgets(g)
      repaintPlots(g)
    }
  }
}
