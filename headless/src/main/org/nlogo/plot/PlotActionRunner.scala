// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.ActionRunner

trait PlotActionRunner extends ActionRunner[PlotAction] {

  def getPlot(name: String): Option[Plot]
  def getPlotPen(plotName: String, penName: String): Option[PlotPen]

  override def run(action: PlotAction) = action match {
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

class BasicPlotActionRunner(plots: Seq[Plot]) extends PlotActionRunner {
  override def getPlot(name: String) =
    plots.find(_.name.equalsIgnoreCase(name))
  override def getPlotPen(plotName: String, penName: String) =
    getPlot(plotName).flatMap(_.getPen(penName))
}
