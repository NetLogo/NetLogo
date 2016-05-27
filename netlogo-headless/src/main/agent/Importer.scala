// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// Importer is too big a class to convert to Scala all at once, so
// we'll convert it at method at a time, as needed, by relocating
// methods from ImporterJ to here. - ST 7/11/12

import org.nlogo.{ api, core },
  core.{PlotPenState, PlotPenInterface, Breed, AgentKind, AgentVariables},
  api.{PlotState, PlotInterface, ImporterUser}

import collection.immutable.ListMap
import collection.JavaConverters._
import ImporterJ.Junk

class Importer(_errorHandler: ImporterJ.ErrorHandler,
               _world: World,
               _importerUser: ImporterUser,
               _stringReader: ImporterJ.StringReader)
extends ImporterJ(_errorHandler, _world, _importerUser, _stringReader) {

  override def getImplicitVariables(kind: AgentKind): Array[String] =
    kind match {
      case AgentKind.Observer =>
        AgentVariables.implicitObserverVariables.toArray
      case AgentKind.Turtle =>
        AgentVariables.implicitTurtleVariables.toArray
      case AgentKind.Patch =>
        AgentVariables.implicitPatchVariables.toArray
      case AgentKind.Link =>
        AgentVariables.implicitLinkVariables.toArray
    }

  def getSpecialObserverVariables: Array[String] = {
    import ImporterJ._
    Array(
      MIN_PXCOR_HEADER, MAX_PXCOR_HEADER, MIN_PYCOR_HEADER, MAX_PYCOR_HEADER,
      SCREEN_EDGE_X_HEADER, SCREEN_EDGE_Y_HEADER,
      PERSPECTIVE_HEADER, SUBJECT_HEADER,
      NEXT_INDEX_HEADER, DIRECTED_LINKS_HEADER, TICKS_HEADER)
  }

  def getSpecialTurtleVariables: Array[String] = {
    val vars = AgentVariables.implicitTurtleVariables
    Array(vars(Turtle.VAR_WHO), vars(Turtle.VAR_BREED),
          vars(Turtle.VAR_LABEL), vars(Turtle.VAR_SHAPE))
  }

  def getSpecialPatchVariables: Array[String] = {
    val vars = AgentVariables.implicitPatchVariables
    Array(vars(Patch.VAR_PXCOR), vars(Patch.VAR_PYCOR),
          vars(Patch.VAR_PLABEL))
  }

  def getSpecialLinkVariables: Array[String] = {
    val vars = AgentVariables.implicitLinkVariables
    Array(vars(Link.VAR_BREED), vars(Link.VAR_LABEL),
          vars(Link.VAR_END1), vars(Link.VAR_END2))
  }

  def getAllVars(breeds: ListMap[String, Breed]): java.util.List[String] =
    breeds.values.flatMap(_.owns).toSeq.asJava

  /// plots

  def importPlots() {
    if (hasMoreLines(false)) {
      val firstLine = nextLine()
      val currentPlot = firstLine(0)
      if (currentPlot.nonEmpty)
        importerUser.currentPlot(currentPlot)
      while (hasMoreLines(false)) {
        val line = nextLine()
        try {
          val plotName = getTokenValue(line(0), false, false).asInstanceOf[String]
          val plot = importerUser.getPlot(plotName)
          if (plot == null) {
            errorHandler.showError("Error Importing Plots",
                "The plot \"" + plotName + "\" does not exist.",
                false)
            // gobble up remaining lines of this section
            while (hasMoreLines(false)) { }
            return
          } else {
            val numPens = importIntro(plot)
            importPens(plot, numPens)
            importPoints(plot)
          }
        }
        catch { case e: ClassCastException =>
          throw new ImporterJ.AbortingImportException(
            ImporterJ.ImportError.ILLEGAL_CLASS_CAST_ERROR, "")
        }
      }
    }
  }

  def importIntro(plot: PlotInterface): Int =
    // this is the header line and we don't really care about it since
    // we have to set everything by hand anyway.
    if (!hasMoreLines(false))
      0
    else if (!hasMoreLines(false))
      0
    else {
      val line = nextLine()
      plot.state = PlotState(
        xMin = readNumber(line(0)),
        xMax = readNumber(line(1)),
        yMin = readNumber(line(2)),
        yMax = readNumber(line(3)),
        autoPlotOn = readBoolean(line(4)))
      plot.currentPenByName_=(readString(line(5)))
      plot.legendIsOpen_=(readBoolean(line(6)))
      readNumber(line(7)).toInt
    }

  def importPens(plot: PlotInterface, numPens: Int) {
    if (hasMoreLines(false))
      for (i <- 0 until numPens; if hasMoreLines(false)) {
        val line = nextLine()
        getTokenValue(line(0), false, false) match {
          case _: Junk =>
            return
          case name: String =>
            plot.getPen(name) match {
              case Some(pen) =>
                pen.state = PlotPenState(
                  isDown = readBoolean(line(1)),
                  mode = readNumber(line(2)).toInt,
                  interval = readNumber(line(3)),
                  color = org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(
                    readNumber(line(4))),
                  x = readNumber(line(5)))
              case None =>
                errorHandler.showError(
                  "Error Importing Plots",
                  "The pen \"" + name + "\" does not exist.", false)
                while (hasMoreLines(false)) {
                  nextLine()
                }
            }
        }
      }
  }

  def importPoints(plot: PlotInterface) {
    if (hasMoreLines(false)) {
      val line = nextLine()
      val penCount = ((line.size - 1) / 4) + 1
      val pens = Array.tabulate(penCount)(i =>
        readString(line(i * 4)))
      if (hasMoreLines(false))
        while (hasMoreLines(true)) {
          val data = nextLine()
          for (i <- 0 until pens.size) {
            plot.getPen(pens(i)) match {
              case Some(pen) =>
                plot.currentPenByName_=(pen.name)
                // there may be blank fields in the list of points
                // since some pens may have more points than others.
                if (data(i * 4).nonEmpty)
                  importPointHelper(plot, pen, data, i)
              case None =>
                errorHandler.showError(
                  "Error Importing Plots",
                  "The pen \"" + pens(i) + "\" does not exist.", false)
            }
          }
        }
    }
  }

  private def importPointHelper(plot: PlotInterface, pen: PlotPenInterface, data: Array[String], i: Int) {
    try {
      pen.state = pen.state.copy(
        color = org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(
          readNumber(data(i * 4 + 2)).toInt),
        isDown = readBoolean(data(i * 4 + 3)))
      plot.plot(
        x = readNumber(data(i * 4)),
        y = readNumber(data(i * 4 + 1)))
    }
    catch { case e: ClassCastException =>
        errorHandler.showError("Import Error",
          "Error while importing " + plot.name +
            ", this point will be skipped.", false)
    }
  }

  private def readNumber(line: String) =
    getTokenValue(line, false, false) match {
      case d: java.lang.Double =>
        d.doubleValue
      case _: Junk =>
        0
    }

  private def readBoolean(line: String) =
    getTokenValue(line, false, false) match {
      case b: java.lang.Boolean =>
        b.booleanValue
      case _: Junk =>
        false
    }

  private def readString(line: String) =
    getTokenValue(line, false, false) match {
      case s: String =>
        s
      case _: Junk =>
        null
    }

}
