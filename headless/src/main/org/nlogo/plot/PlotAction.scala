package org.nlogo.plot

import scala.collection.mutable.Publisher
import scala.collection.mutable.Subscriber

sealed trait PlotAction

object PlotAction extends Publisher[PlotAction] {

  def forward(action: PlotAction) = {
    println(action)
    publish(action)
  }

  case object ClearAll
    extends PlotAction
  case class ClearPlot(plot: Plot)
    extends PlotAction
  case class PlotY(plot: Plot, pen: PlotPen, y: Double)
    extends PlotAction
  case class PlotXY(plot: Plot, pen: PlotPen, x: Double, y: Double)
    extends PlotAction
  case class Histogram(plot: Plot, pen: PlotPen, values: Seq[Double])
    extends PlotAction
  case class AutoPlot(plot: Plot, on: Boolean)
    extends PlotAction
  case class SetRange(plot: Plot, isX: Boolean, min: Double, max: Double)
    extends PlotAction
  case class PenDown(pen: PlotPen, down: Boolean)
    extends PlotAction
  case class HidePen(pen: PlotPen, hidden: Boolean)
    extends PlotAction
  case class ResetPen(pen: PlotPen)
    extends PlotAction
  case class SetPenInterval(pen: PlotPen, interval: Double)
    extends PlotAction
  case class SetPenMode(pen: PlotPen, mode: Int)
    extends PlotAction
  case class SetPenColor(pen: PlotPen, color: Int)
    extends PlotAction
  case class CreateTemporaryPen(plot: Plot, name: String)
    extends PlotAction
}

trait PlotRunner
  extends Subscriber[PlotAction, PlotAction.Pub] {
  self: PlotManager =>

  PlotAction.subscribe(this)

  def notify(pub: PlotAction.Pub, action: PlotAction) {
    run(action)
  }

  def run(action: PlotAction) = action match {
    case PlotAction.ClearAll =>
      clearAll()
    case PlotAction.ClearPlot(plot) =>
      plot.clear()
    case PlotAction.PlotY(plot, pen, y) =>
      plot.plot(pen, y)
    case PlotAction.PlotXY(plot, pen, x, y) =>
      plot.plot(pen, x, y)
    case PlotAction.Histogram(plot, pen, values) =>
      plot.beginHistogram(pen)
      values.foreach(plot.nextHistogramValue)
      plot.endHistogram(pen)
    case PlotAction.AutoPlot(plot, on) =>
      plot.state = plot.state.copy(autoPlotOn = on)
    case PlotAction.SetRange(plot, isX, min, max) =>
      plot.state =
        if (isX)
          plot.state.copy(xMin = min, xMax = max)
        else
          plot.state.copy(yMin = min, yMax = max)
    case PlotAction.PenDown(pen, down) =>
      pen.state = pen.state.copy(isDown = down)
    case PlotAction.HidePen(pen, hidden) =>
      pen.state = pen.state.copy(hidden = hidden)
    case PlotAction.ResetPen(pen) =>
      pen.hardReset()
    case PlotAction.SetPenInterval(pen, interval) =>
      pen.state = pen.state.copy(interval = interval)
    case PlotAction.SetPenMode(pen, mode) =>
      pen.state = pen.state.copy(mode = mode)
    case PlotAction.SetPenColor(pen, color) =>
      pen.state = pen.state.copy(color = color)
    case PlotAction.CreateTemporaryPen(plot, name) =>
      plot.currentPen = plot.getPen(name).getOrElse(plot.createPlotPen(name, true))
  }

}
