// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.{ CSV, PlotInterface, PlotPointInterface }
import collection.mutable.Buffer

// This would be called just `PlotExporter` but we need to leave the class with
// that name and a similar constructor in place in GUI for deprecation.
// -Jeremy B November 2020
class CorePlotExporter(private val plot: PlotInterface, private val csv: CSV) {
  def `export`(writer: java.io.PrintWriter): Unit = {
    exportIntro(writer)
    exportPens(writer)
    exportPoints(writer)
  }

  private def exportIntro(writer: java.io.PrintWriter): Unit = {
    writer.println(csv.data(plot.name))
    writer.println(csv.headerRow(Array(
      "x min", "x max", "y min", "y max",
      "autoplot?", "current pen",
      "legend open?",
      "number of pens")))
    writer.println(csv.dataRow(Array(
      Double.box(plot.state.xMin),
      Double.box(plot.state.xMax),
      Double.box(plot.state.yMin),
      Double.box(plot.state.yMax),
      Boolean.box(plot.state.autoPlotX && plot.state.autoPlotY),
      plot.currentPen.map(_.name).getOrElse(""),
      Boolean.box(plot.legendIsOpen),
      Int.box(plot.pens.size))))
    writer.println()
  }


  private def exportPens(writer: java.io.PrintWriter): Unit = {
    writer.println(csv.headerRow(Array(
      "pen name", "pen down?", "mode", "interval",
      "color", "x")))
    for (pen <- plot.pens) {
      writer.println(csv.dataRow(Array(
        pen.name,
        Boolean.box(pen.state.isDown),
        Int.box(pen.state.mode),
        Double.box(pen.state.interval),
        org.nlogo.api.Color.argbToColor(pen.state.color),
        Double.box(pen.state.x))))
    }
    writer.println()
  }

  private def exportPoints(writer: java.io.PrintWriter): Unit = {
    /// Output header row of pen names
    var numPens = 0
    for (pen <- plot.pens) {
      if (numPens > 0) {writer.print(",,,,")}
      writer.print(csv.data(pen.name))
      numPens += 1
    }
    writer.println()

    // Output data row headers
    val pointHeaders = Buffer[String]()
    for (i <- 0 until numPens)
      pointHeaders ++= Array("x", "y", "color", "pen down?")
    writer.println(csv.headerRow(pointHeaders.toArray))

    // Output data rows
    // Get pen data lists, put them in a list,
    // and transpose that list into a 2 dimensional array
    val penDataListsList = Buffer[Seq[PlotPointInterface]]()
    var maxPenDataListSize = 0
    for (pen <- plot.pens) {
      val penDataList = Option(pen.points).getOrElse(Vector())
      maxPenDataListSize = maxPenDataListSize max penDataList.size
      penDataListsList += penDataList
    }
    val outColumnsArray:Array[Array[PlotPointInterface]] = Array.ofDim(maxPenDataListSize, numPens)
    for (i <- 0 until numPens) {
      val penDataList = penDataListsList(i)
      val penDataListSize = penDataList.size
      for (j <- 0 until penDataListSize)
        outColumnsArray(j)(i) = penDataList(j)
    }
    // Now output the 2 dimensional array of PlotPoints
    for (row <- 0 until maxPenDataListSize) {
      for (col <- 0 until numPens) {
        if (outColumnsArray(row)(col) != null) {
          writer.print(csv.data(outColumnsArray(row)(col).x))
          writer.print(",")
          writer.print(csv.data(outColumnsArray(row)(col).y))
          writer.print(",")
          writer.print(csv.data(org.nlogo.api.Color.argbToColor(outColumnsArray(row)(col).color)))
          writer.print(",")
          writer.print(csv.data(outColumnsArray(row)(col).isDown))
          if (col + 1 < numPens) {writer.print(",")}
        }
        else {
          if (col + 1 < numPens) writer.print(",,,,")
          else writer.print(",,,")
        }
      }
      writer.println()
    }
  }
}
