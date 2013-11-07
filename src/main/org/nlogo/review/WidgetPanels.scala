package org.nlogo.review

import scala.collection.JavaConverters._

import org.nlogo.mirror.FixedViewSettings
import org.nlogo.mirror.ModelRun
import org.nlogo.window.GUIWorkspace
import org.nlogo.window.PlotWidget

import javax.swing.JPanel

object WidgetPanels {

  def create(ws: GUIWorkspace, run: ModelRun): Seq[JPanel] = {
    newPlotPanels(ws, run) :+ newViewWidgetPanel(ws, run)
  }

  private def newViewWidgetPanel(ws: GUIWorkspace, run: ModelRun) = {
    val container = ws.viewWidget.findWidgetContainer
    val viewWidgetBounds = container.getUnzoomedBounds(ws.viewWidget)
    val viewWidgetPanel = new ViewWidgetPanel(viewWidgetBounds)

    val viewBounds = container.getUnzoomedBounds(ws.view)
    val viewSettings = FixedViewSettings(ws.view)
    viewWidgetPanel.add(new ViewPanel(run, viewBounds, viewSettings))
    viewWidgetPanel
  }

  private def newPlotPanels(ws: GUIWorkspace, run: ModelRun): Seq[PlotPanel] = {
    val container = ws.viewWidget.findWidgetContainer
    container.getWidgetsForSaving.asScala.collect {
      case plotWidget: PlotWidget =>
        new PlotPanel(
          run,
          plotWidget.plot,
          container.getUnzoomedBounds(plotWidget),
          plotWidget,
          plotWidget.gui.legend.open)
    }
  }
}