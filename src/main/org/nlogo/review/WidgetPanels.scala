package org.nlogo.review

import org.nlogo.mirror.FixedViewSettings
import org.nlogo.mirror.ModelRun
import org.nlogo.swing.Utils.createWidgetBorder
import org.nlogo.window.GUIWorkspace
import org.nlogo.window.MonitorWidget
import org.nlogo.window.PlotWidget
import org.nlogo.window.WidgetContainer

import javax.swing.JPanel

trait WidgetPanel extends JPanel {
  val panelBounds: java.awt.Rectangle
  val originalFont: java.awt.Font
  setBounds(panelBounds)
  setBorder(createWidgetBorder)
  setFont(originalFont)
}

object WidgetPanels {

  def create(ws: GUIWorkspace, run: ModelRun): Seq[JPanel] = {
    val container = ws.viewWidget.findWidgetContainer
    newViewWidgetPanel(ws, container, run) +:
      workspaceWidgets(ws).zipWithIndex.collect {
        case (w: PlotWidget, _) => newPlotPanel(container, w, run)
        case (w: MonitorWidget, i) => newMonitorPanel(container, w, run, i)
      }
  }

  def newViewWidgetPanel(
    ws: GUIWorkspace,
    container: WidgetContainer,
    run: ModelRun): ViewWidgetPanel = {
    val viewSettings = FixedViewSettings(ws.view)
    val viewWidgetBounds = container.getUnzoomedBounds(ws.viewWidget)
    val viewBounds = container.getUnzoomedBounds(ws.view)
    new ViewWidgetPanel(
      run,
      viewWidgetBounds,
      viewBounds,
      viewSettings)
  }

  def newMonitorPanel(
    container: WidgetContainer,
    monitorWidget: MonitorWidget,
    run: ModelRun,
    index: Int): MonitorPanel = {
    new MonitorPanel(
      container.getUnzoomedBounds(monitorWidget),
      monitorWidget.originalFont,
      monitorWidget.displayName,
      run,
      index)
  }

  def newPlotPanel(
    container: WidgetContainer,
    plotWidget: PlotWidget,
    run: ModelRun): PlotPanel = {
    new PlotPanel(
      container.getUnzoomedBounds(plotWidget),
      plotWidget.originalFont,
      run,
      plotWidget.plot,
      plotWidget.gui.legend.open)
  }
}
