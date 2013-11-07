package org.nlogo.review

import scala.collection.JavaConverters._

import org.nlogo.mirror.ModelRun
import org.nlogo.window.GUIWorkspace
import org.nlogo.window.PlotWidget

import javax.swing.JPanel

object WidgetPanels {

  def create(ws: GUIWorkspace, run: ModelRun): Seq[JPanel] = {
    newPlotPanels(ws, run) :+ new ViewPanel(run)
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