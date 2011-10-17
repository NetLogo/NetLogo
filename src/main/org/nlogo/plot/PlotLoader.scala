// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.Color.translateSavedColor
import org.nlogo.api.StringUtils.unEscapeString

object PlotLoader {

  def parsePlot(widget: Array[String], plot: Plot, autoConverter: String => String) {
    val (plotLines, penLines) = widget.toList.span(_ != "PENS")
    def convert(os:Option[String]) = os.map(autoConverter).getOrElse("")

    plot.name(plotLines(5))
    if (11 < plotLines.length) {
      plot.defaultXMin = plotLines(8).toDouble
      plot.defaultXMax = plotLines(9).toDouble
      plot.defaultYMin = plotLines(10).toDouble
      plot.defaultYMax = plotLines(11).toDouble
    }
    if (12 < plotLines.length)
      plot.defaultAutoPlotOn = plotLines(12).toBoolean
    if (14 < plotLines.length) {
      parseStringLiterals(plotLines(14)) match {
        case Nil => // old style model, no new plot code. this is ok.
        case setup :: update :: Nil =>
          // the correct amount of plot code.
          plot.setupCode = convert(setup)
          plot.updateCode = convert(update)
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
        val pen = plot.createPlotPen(spec.name, false, convert(spec.setupCode), convert(spec.updateCode))
        pen.defaultInterval = spec.interval
        pen.defaultMode = spec.mode
        pen.defaultColor = if (translateColors) translateSavedColor(spec.color) else spec.color
        pen.inLegend = spec.inLegend
      }
    }
    loadPens(doubleCheckedPenLines, translateColors=false)
    plot.clear()
  }

  // example pen line: "My Pen" 1.0 0 -16777216 true
  // name, default interval, mode, color, legend
  private[plot] case class PenSpec(name: String, interval: Double, mode: Int, color: Int, inLegend: Boolean,
                                   setupCode: Option[String], updateCode: Option[String])

  private[plot] def parsePen(s: String): PenSpec = {
    // the drop(1) skips the opening quote
    val tokens = tokenize(s.drop(1)).toList

    // this is a bit messy. span() puts the last part of the name in the wrong
    // part of the result, so we have to shuffle a token from one list to the other
    val (nameTokens, moreTokens) =
    spanPlusOne(tokens)(tok => !tok.endsWith("\"") || tok.endsWith("\\\""))

    // dropRight drops the closing quote
    val name = unEscapeString(nameTokens.mkString.dropRight(1))

    // the rest of the line is easy to handle
    val (interval, mode, color, inLegend) =
    moreTokens.filter(_.trim.nonEmpty) match {
      case List(interval, mode, color, inLegend, _*) =>
        if (!PlotPen.isValidPlotPenMode(mode.toInt))
          sys.error(mode + " is not a valid plot pen mode")
        (interval.toDouble, mode.toInt, color.toInt, inLegend.toBoolean)
      case _ =>
        sys.error("bad line: \"" + s + "\"")
    }

    val codeString = moreTokens.dropWhile(!_.startsWith("\"")).mkString.trim
    parseStringLiterals(codeString) match {
      case List(setup, update) => PenSpec(name, interval, mode, color, inLegend, setup, update)
      case _ => PenSpec(name, interval, mode, color, inLegend, None, None)
    }
  }

  // Used to parse a line that may contain multiple string literals, surrounded by double quotes
  // and separated by spaces.  This is tricky because the string literals may contain escaped
  // double quotes, so it's nontrivial to figure out where one literal ends and the next starts.
  // (right now this doesn't fail properly when the first quote in the code string is missing.
  // it just thinks there is no code, and returns None. not sure if it's worth fixing.)
  private[plot] def parseStringLiterals(s: String): List[Option[String]] = {
    def toCodeOption(s: String) = {
      val code = unEscapeString(s.trim.drop(1).dropRight(1))
      if (code.nonEmpty) Some(code)
      else None
    }
    def isCloseQuote(tok: String) =
      tok.endsWith("\"") && !tok.endsWith("\\\"")
    def recurse(toks: List[String]): List[String] =
      if(toks.isEmpty) Nil
      else {
        val (xs, more) = spanPlusOne(toks)(!isCloseQuote(_))
        xs.mkString :: recurse(more)
      }
    val tokens = tokenize(s).toList
    if (tokens.isEmpty) Nil
    else recurse(tokens).map(toCodeOption(_))
  }

  // encapsulate ugly StringTokenizer
  private def tokenize(s: String): Iterator[String] = {
    import java.util.StringTokenizer
    val tokenizer = new StringTokenizer(s, " ", true)
    new Iterator[String]() {
      def hasNext = tokenizer.hasMoreTokens
      def next() = tokenizer.nextToken()
    }
  }

  // like span, but keeps the first failing item
  private def spanPlusOne[T](list: List[T])(pred: T => Boolean) =
    list.span(pred) match {
      case (xs, y :: ys) => (xs :+ y, ys)
      case (xs, Nil) => (xs, Nil)
    }
}
