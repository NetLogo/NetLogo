// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.CSV
import collection.mutable.Buffer

class PlotExporter(private val plot: Plot, private val csv: CSV) {
  def export(writer: java.io.PrintWriter) {
    exportIntro(writer)
    exportPens(writer)
    exportPoints(writer)
  }

  private def exportIntro(writer: java.io.PrintWriter) {
    writer.println(csv.data(plot.name))
    writer.println(csv.headerRow(Array(
      "x min", "x max", "y min", "y max",
      "autoplot?", "current pen",
      "legend open?",
      "number of pens")))
    writer.println(csv.dataRow(Array(
      double2Double(plot.xMin),
      double2Double(plot.xMax),
      double2Double(plot.yMin),
      double2Double(plot.yMax),
      boolean2Boolean(plot.autoPlotOn),
      plot.currentPen.map(_.name).getOrElse(""),
      boolean2Boolean(plot.legendIsOpen),
      int2Integer(plot.pens.size))))
    writer.println()
  }


  private def exportPens(writer: java.io.PrintWriter) {
    writer.println(csv.headerRow(Array(
      "pen name", "pen down?", "mode", "interval",
      "color", "x")))
    for (pen <- plot.pens) {
      writer.println(csv.dataRow(Array(
        pen.name,
        boolean2Boolean(pen.isDown),
        int2Integer(pen.mode),
        double2Double(pen.interval),
        org.nlogo.api.Color.argbToColor(pen.color),
        double2Double(pen.x))))
    }
    writer.println()
  }

  private def exportPoints(writer: java.io.PrintWriter) {
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
      pointHeaders += ("x", "y", "color", "pen down?")
    writer.println(csv.headerRow(pointHeaders.toArray))

    // Output data rows
    // Get pen data lists, put them in a list,
    // and transpose that list into a 2 dimensional array
    var penDataListsList = Buffer[Seq[PlotPoint]]()
    var maxPenDataListSize = 0
    for (pen <- plot.pens) {
      var penDataList = pen.points
      if (penDataList == null)
        penDataList = Buffer()
      maxPenDataListSize = maxPenDataListSize max penDataList.size
      penDataListsList += penDataList
    }
    val outColumnsArray:Array[Array[PlotPoint]] = Array.ofDim(maxPenDataListSize, numPens)
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
