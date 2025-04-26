// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.core.{Plot => ParsedPlot, Pen => ParsedPen}

object PlotLoader {

  def loadPlot(parsedPlot: ParsedPlot, plot: Plot): Unit = {
    plot.name(parsedPlot.display.getOrElse(""))
    plot.defaultState = plot.defaultState.copy(
        xMin = parsedPlot.xmin,
        xMax = parsedPlot.xmax,
        yMin = parsedPlot.ymin,
        yMax = parsedPlot.ymax,
        autoPlotX = parsedPlot.autoPlotX,
        autoPlotY = parsedPlot.autoPlotY)
    plot.setupCode = parsedPlot.setupCode
    plot.updateCode = parsedPlot.updateCode
    plot.legendIsOpen = parsedPlot.legendOn

    def loadPens(parsedPens: Seq[ParsedPen]): Unit = {
      plot.pens = Nil
      for (parsedPen <- parsedPens) {
        val pen = plot.createPlotPen(parsedPen.display, false,
                                     parsedPen.setupCode,
                                     parsedPen.updateCode)
        pen.defaultState = pen.defaultState.copy(
          interval = parsedPen.interval,
          mode = parsedPen.mode,
          color = parsedPen.color)
        pen.inLegend = parsedPen.inLegend
      }
    }
    loadPens(parsedPlot.pens)
    plot.clear()
  }
}
