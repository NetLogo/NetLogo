// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// Importer is too big a class to convert to Scala all at once, so
// we'll convert it at method at a time, as needed, by relocating
// methods from ImporterJ to here. - ST 7/11/12

import org.nlogo.api
import api.{ AgentKind, AgentVariables, Breed, ImporterUser, PlotInterface }
import collection.immutable.ListMap
import collection.JavaConverters._

class Importer(_errorHandler: ImporterJ.ErrorHandler,
               _world: World,
               _importerUser: ImporterUser,
               _stringReader: ImporterJ.StringReader)
extends ImporterJ(_errorHandler, _world, _importerUser, _stringReader) {

  override def getImplicitVariables(kind: AgentKind): Array[String] =
    kind match {
      case AgentKind.Observer =>
        AgentVariables.getImplicitObserverVariables.toArray
      case AgentKind.Turtle =>
        AgentVariables.getImplicitTurtleVariables(world.program.is3D).toArray
      case AgentKind.Patch =>
        AgentVariables.getImplicitPatchVariables(world.program.is3D).toArray
      case AgentKind.Link =>
        AgentVariables.getImplicitLinkVariables.toArray
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
    val vars = AgentVariables.getImplicitTurtleVariables(false)
    Array(vars(Turtle.VAR_WHO), vars(Turtle.VAR_BREED),
          vars(Turtle.VAR_LABEL), vars(Turtle.VAR_SHAPE))
  }

  def getSpecialPatchVariables: Array[String] = {
    val vars = AgentVariables.getImplicitPatchVariables(false)
    Array(vars(Patch.VAR_PXCOR), vars(Patch.VAR_PYCOR),
          vars(Patch.VAR_PLABEL))
  }

  def getSpecialLinkVariables: Array[String] = {
    val vars = AgentVariables.getImplicitLinkVariables
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

  def importIntro(plot: PlotInterface): Int = {
    // this is the header line and we don't really care about it since
    // we have to set everything by hand anyway.
    if (!hasMoreLines(false) || !hasMoreLines(false))
      0
    else {
      val line = nextLine()
      plot.xMin_=(readNumber(line(0)))
      plot.xMax_=(readNumber(line(1)))
      plot.yMin_=(readNumber(line(2)))
      plot.yMax_=(readNumber(line(3)))
      plot.autoPlotOn_=(readBoolean(line(4)))
      plot.currentPen_=(readString(line(5)))
      plot.legendIsOpen_=(readBoolean(line(6)))
      readNumber(line(7)).toInt
    }
  }

  def importPens(plot: api.PlotInterface, numPens: Int) {
    if (hasMoreLines(false))
      for (i <- 0 until numPens; if hasMoreLines(false)) {
        val line = nextLine()
        getTokenValue(line(0), false, false) match {
          case _: Junk =>
            return
          case name: String =>
            plot.getPen(name) match {
              case Some(pen) =>
                pen.isDown_=(readBoolean(line(1)))
                pen.mode_=(readNumber(line(2)).toInt)
                pen.interval_=(readNumber(line(3)))
                pen.color_=(
                  org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(
                    readNumber(line(4))))
                pen.x_=(readNumber(line(5)))
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
                // there may be blank fields in the list of points
                // since some pens may have more points than others.
                if (data(i * 4).nonEmpty)
                  try pen.plot(
                    readNumber(data(i * 4)),
                    readNumber(data(i * 4 + 1)),
                    org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(
                      readNumber(data(i * 4 + 2)).toInt),
                    readBoolean(data(i * 4 + 3)))
                  catch { case e: ClassCastException =>
                    errorHandler.showError("Import Error",
                        "Error while importing " + plot.name +
                            ", this point will be skipped.", false)
                  }
              case None =>
                errorHandler.showError(
                  "Error Importing Plots",
                  "The pen \"" + pens(i) + "\" does not exist.", false)
            }
          }
        }
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
