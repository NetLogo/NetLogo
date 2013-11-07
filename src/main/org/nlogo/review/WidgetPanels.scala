package org.nlogo.review

import scala.collection.JavaConverters._

import org.nlogo.mirror.FixedViewSettings
import org.nlogo.mirror.ModelRun
import org.nlogo.window.GUIWorkspace
import org.nlogo.window.PlotWidget

import javax.swing.JPanel

object WidgetPanels {

  def create(ws: GUIWorkspace, run: ModelRun): Seq[JPanel] = {
    newPlotPanels(ws, run) :+ newViewPanel(ws, run)
  }

  private def newViewPanel(ws: GUIWorkspace, run: ModelRun) = {
    val container = ws.viewWidget.findWidgetContainer
    val bounds = container.getUnzoomedBounds(ws.view)
    val settings = FixedViewSettings(ws.view)
    new ViewPanel(run, bounds, settings)
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