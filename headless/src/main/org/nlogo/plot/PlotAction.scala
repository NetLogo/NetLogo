package org.nlogo.plot

import scala.collection.mutable.Publisher

sealed trait PlotAction

object PlotAction  {

  case class ClearPlot(plotName: String)
    extends PlotAction
  case class PlotY(plotName: String, penName: String, y: Double)
    extends PlotAction
  case class PlotXY(plotName: String, penName: String, x: Double, y: Double)
    extends PlotAction
  case class Histogram(plotName: String, penName: String, values: Seq[Double])
    extends PlotAction
  case class AutoPlot(plotName: String, on: Boolean)
    extends PlotAction
  case class SetRange(plotName: String, isX: Boolean, min: Double, max: Double)
    extends PlotAction
  case class PenDown(plotName: String, penName: String, down: Boolean)
    extends PlotAction
  case class HidePen(plotName: String, penName: String, hidden: Boolean)
    extends PlotAction
  case class ResetPen(plotName: String, penName: String)
    extends PlotAction
  case class SetPenInterval(plotName: String, penName: String, interval: Double)
    extends PlotAction
  case class SetPenMode(plotName: String, penName: String, mode: Int)
    extends PlotAction
  case class SetPenColor(plotName: String, penName: String, color: Int)
    extends PlotAction
  case class CreateTemporaryPen(plotName: String, penName: String)
    extends PlotAction
}

trait PlotRunner {

  def getPlot(name: String): Option[Plot]
  def getPlotPen(plotName: String, penName: String): Option[PlotPen]

  def run(action: PlotAction) = action match {
    case PlotAction.ClearPlot(plotName) =>
      for { plot <- getPlot(plotName) }
        plot.clear()
    case PlotAction.PlotY(plotName, penName, y) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } plot.plot(pen, y)
    case PlotAction.PlotXY(plotName, penName, x, y) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } plot.plot(pen, x, y)
    case PlotAction.Histogram(plotName, penName, values) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } {
        plot.beginHistogram(pen)
        values.foreach(plot.nextHistogramValue)
        plot.endHistogram(pen)
      }
    case PlotAction.AutoPlot(plotName, on) =>
      for { plot <- getPlot(plotName) }
        plot.state = plot.state.copy(autoPlotOn = on)
    case PlotAction.SetRange(plotName, isX, min, max) =>
      for { plot <- getPlot(plotName) }
        plot.state =
          if (isX)
            plot.state.copy(xMin = min, xMax = max)
          else
            plot.state.copy(yMin = min, yMax = max)
    case PlotAction.PenDown(plotName, penName, down) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } pen.state = pen.state.copy(isDown = down)
    case PlotAction.HidePen(plotName, penName, hidden) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } pen.state = pen.state.copy(hidden = hidden)
    case PlotAction.ResetPen(plotName, penName) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } pen.hardReset()
    case PlotAction.SetPenInterval(plotName, penName, interval) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } pen.state = pen.state.copy(interval = interval)
    case PlotAction.SetPenMode(plotName, penName, mode) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } pen.state = pen.state.copy(mode = mode)
    case PlotAction.SetPenColor(plotName, penName, color) =>
      for {
        plot <- getPlot(plotName)
        pen <- plot.getPen(penName)
      } pen.state = pen.state.copy(color = color)
    case PlotAction.CreateTemporaryPen(plotName, penName) =>
      for {
        plot <- getPlot(plotName)
        pen = plot.getPen(penName).getOrElse(plot.createPlotPen(penName, true))
      } plot.currentPen = pen
  }

}
