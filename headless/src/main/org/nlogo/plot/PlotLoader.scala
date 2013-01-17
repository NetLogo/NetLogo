// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api
import api.StringUtils.unEscapeString

object PlotLoader {

  def parsePlot(widget: Array[String], plot: Plot) {
    val (plotLines, penLines) =
      widget.toList.span(_ != "PENS")
    plot.name(plotLines(5))
    if (11 < plotLines.length)
      plot.defaultState = plot.defaultState.copy(
        xMin = plotLines(8).toDouble,
        xMax = plotLines(9).toDouble,
        yMin = plotLines(10).toDouble,
        yMax = plotLines(11).toDouble)
    if (12 < plotLines.length)
      plot.defaultState = plot.defaultState.copy(
        autoPlotOn = plotLines(12).toBoolean)
    if (14 < plotLines.length) {
      parseStringLiterals(plotLines(14)) match {
        case Nil => // old style model, no new plot code. this is ok.
        case setup :: update :: Nil =>
          // the correct amount of plot code.
          plot.setupCode = unEscapeString(setup)
          plot.updateCode = unEscapeString(update)
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

    def loadPens(penLines: Seq[String]) {
      plot.pens = Nil
      for (spec <- penLines.map(parsePen)) {
        val pen = plot.createPlotPen(spec.name, false,
                                     spec.setupCode,
                                     spec.updateCode)
        pen.defaultState = pen.defaultState.copy(
          interval = spec.interval,
          mode = spec.mode,
          color = spec.color)
        pen.inLegend = spec.inLegend
      }
    }
    loadPens(doubleCheckedPenLines)
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
    require(api.PlotPenInterface.isValidPlotPenMode(mode.toInt))
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
