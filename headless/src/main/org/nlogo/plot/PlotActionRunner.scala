// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.ActionRunner
import PlotAction._

trait PlotActionRunner extends ActionRunner[PlotAction] {

  def getPlot(name: String): Option[Plot]
  def getPlotPen(plotName: String, penName: String): Option[PlotPen]

  def withPlot(plotName: String)(f: (Plot) => Unit) =
    getPlot(plotName).foreach(f)

  def withPen(plotName: String, penName: String)(f: (PlotPen) => Unit) =
    getPlotPen(plotName, penName).foreach(f)

  def withPlotAndPen(plotName: String, penName: String)(f: (Plot, PlotPen) => Unit) =
    for {
      plot <- getPlot(plotName)
      pen <- plot.getPen(penName)
    } f(plot, pen)

  override def run(action: PlotAction) = {

    // any action on a plot makes it dirty
    withPlot(action.plotName) {
      _.dirty = true
    }

    action match {

      case ClearPlot(plotName) =>
        withPlot(plotName) {
          _.clear()
        }

      case PlotY(plotName, penName, y) =>
        withPlotAndPen(plotName, penName) {
          _.plot(_, y)
        }

      case PlotXY(plotName, penName, x, y) =>
        withPlotAndPen(plotName, penName) {
          _.plot(_, x, y)
        }

      case AutoPlot(plotName, on) =>
        withPlot(plotName) { plot =>
          plot.state = plot.state.copy(autoPlotOn = on)
        }

      case SetRange(plotName, isX, min, max) =>
        withPlot(plotName) { plot =>
          plot.state =
            if (isX)
              plot.state.copy(xMin = min, xMax = max)
            else
              plot.state.copy(yMin = min, yMax = max)
        }

      case PenDown(plotName, penName, down) =>
        withPen(plotName, penName) { pen =>
          pen.state = pen.state.copy(isDown = down)
        }

      case HidePen(plotName, penName, hidden) =>
        withPen(plotName, penName) { pen =>
          pen.state = pen.state.copy(hidden = hidden)
        }

      case HardResetPen(plotName, penName) =>
        withPen(plotName, penName) {
          _.hardReset()
        }

      case SoftResetPen(plotName, penName) =>
        withPen(plotName, penName) {
          _.softReset()
        }

      case SetPenInterval(plotName, penName, interval) =>
        withPen(plotName, penName) { pen =>
          pen.state = pen.state.copy(interval = interval)
        }

      case SetPenMode(plotName, penName, mode) =>
        withPen(plotName, penName) { pen =>
          pen.state = pen.state.copy(mode = mode)
        }

      case SetPenColor(plotName, penName, color) =>
        withPen(plotName, penName) { pen =>
          pen.state = pen.state.copy(color = color)
        }

      case CreateTemporaryPen(plotName, penName) =>
        withPlot(plotName) { plot =>
          plot.currentPen = plot
            .getPen(penName)
            .getOrElse(plot.createPlotPen(penName, true))
        }
    }
  }

}

class BasicPlotActionRunner(plots: Seq[Plot]) extends PlotActionRunner {
  override def getPlot(name: String) =
    plots.find(_.name.equalsIgnoreCase(name))
  override def getPlotPen(plotName: String, penName: String) =
    getPlot(plotName).flatMap(_.getPen(penName))
}
