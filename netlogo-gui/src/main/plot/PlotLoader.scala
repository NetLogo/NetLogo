// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.core.{ Pen => CorePen, Plot => CorePlot }
import org.nlogo.api.Color.translateSavedColor
import org.nlogo.api.StringUtils.unEscapeString

object PlotLoader {

  def loadPlot(corePlot: CorePlot, plot: Plot): Plot = {
    plot.name = corePlot.display.getOrElse("")
    plot.defaultXMin = corePlot.xmin
    plot.defaultXMax = corePlot.xmax
    plot.defaultYMax = corePlot.ymax
    plot.defaultYMin = corePlot.ymin
    plot.defaultAutoPlotX = corePlot.autoPlotX
    plot.defaultAutoPlotY = corePlot.autoPlotY
    plot.setupCode = corePlot.setupCode
    plot.updateCode = corePlot.updateCode
    plot.pens = corePlot.pens.map(loadPen(plot))
    plot.legendIsOpen = corePlot.legendOn
    plot.clear()
    plot
  }

  def loadPen(plot: Plot)(pen: CorePen): PlotPen = {
    val newPen = plot.createPlotPen(pen.display, false, pen.setupCode, pen.updateCode)
    newPen.defaultInterval = pen.interval
    newPen.defaultMode = pen.mode
    newPen.defaultColor = pen.color
    newPen.inLegend = pen.inLegend
    newPen.hardReset()
    newPen
  }

  @deprecated("use loadPlot instead", "hexy")
  def parsePlot(widget: Array[String], plot: Plot, autoConvert: String => String) {
    val (plotLines, penLines) =
      widget.toList.span(_ != "PENS")
    plot.name(plotLines(5))
    if (11 < plotLines.length) {
      plot.defaultXMin = plotLines(8).toDouble
      plot.defaultXMax = plotLines(9).toDouble
      plot.defaultYMin = plotLines(10).toDouble
      plot.defaultYMax = plotLines(11).toDouble
    }
    if (12 < plotLines.length)
      plot.defaultAutoPlotX = plotLines(12).toBoolean // FIX FIX FIX LATER LATER LATER
      plot.defaultAutoPlotY = plotLines(12).toBoolean
    if (14 < plotLines.length) {
      parseStringLiterals(plotLines(14)) match {
        case Nil => // old style model, no new plot code. this is ok.
        case setup :: update :: Nil =>
          // the correct amount of plot code.
          plot.setupCode = autoConvert(unEscapeString(setup))
          plot.updateCode = autoConvert(unEscapeString(update))
        case _ =>
          // 1, or 3 or more bits of code...error.
          sys.error("Plot '" + plot.name + "' contains invalid setup and/or update code: " + plotLines(14))
      }
    }

    // some models might not have a PENS line with any pens underneath.
    // deal with that here.
    val doubleCheckedPenLines = penLines match {
      case "PENS" :: xs => xs
      case _ => Nil
    }

    def loadPens(penLines: Seq[String], translateColors: Boolean) {
      plot.pens = Nil
      for (spec <- penLines.map(parsePen)) {
        val pen = plot.createPlotPen(spec.name, false,
                                     autoConvert(spec.setupCode),
                                     autoConvert(spec.updateCode))
        pen.defaultInterval = spec.interval
        pen.defaultMode = spec.mode
        pen.defaultColor =
          if (translateColors)
            translateSavedColor(spec.color)
          else spec.color
        pen.inLegend = spec.inLegend
      }
    }
    loadPens(doubleCheckedPenLines, translateColors=false)
    plot.clear()
  }

  // example pen line: "My Pen" 1.0 0 -16777216 true
  // name, default interval, mode, color, legend
  private[plot] case class PenSpec(name: String, interval: Double, mode: Int, color: Int, inLegend: Boolean,
                                   setupCode: String, updateCode: String)

  private[plot] def parsePen(s: String): PenSpec = {
    require(s.head == '"')
    val (name, rest) = parseOne(s.tail)
    val (rest1, rest2) = rest.span(_ != '"')
    val List(interval, mode, color, inLegend) =
      rest1.trim.split("\\s+").toList
    require(PlotPen.isValidPlotPenMode(mode.toInt))
    // optional; pre-5.0 models don't have them
    val (setup, update) =
      parseStringLiterals(rest2) match {
        case List(setup, update) =>
          (setup, update)
        case _ =>
          ("", "")
      }
    PenSpec(unEscapeString(name), interval.toDouble, mode.toInt, color.toInt, inLegend.toBoolean,
            unEscapeString(setup), unEscapeString(update))
  }

  // This is tricky because the string literals may contain escaped double quotes, so it's
  // nontrivial to figure out where one literal ends and the next starts.  Assumes the
  // opening double quote has already been detected and discarded.
  private def parseOne(s: String): (String, String) =
    if(s.isEmpty)
      ("", "")
    else if (s.head == '"')
      ("", s.tail.trim)
    else if(s.take(2) == "\\\"")
      parseOne(s.drop(2)) match {
        case (more1, more2) =>
          ('"' + more1, more2)
      }
    else
      parseOne(s.tail) match {
        case (more1, more2) =>
          (s.head + more1, more2)
      }

  // Used to parse a line that may contain multiple string literals, surrounded by double quotes and
  // separated by spaces.
  private[plot] def parseStringLiterals(s: String): List[String] =
    s.headOption match {
      case Some('"') =>
        val (result, more) = parseOne(s.tail)
        result :: parseStringLiterals(more)
      case _ =>
        Nil
    }

}
