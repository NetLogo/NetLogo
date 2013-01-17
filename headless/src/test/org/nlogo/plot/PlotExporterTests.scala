// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.CSV

class PlotExporterTests extends SimplePlotTest {

  val csv = new CSV(_.toString)

  val EXPORT_RESULT =
    "\"test plot\"\n" +
    "\"x min\",\"x max\",\"y min\",\"y max\",\"autoplot?\",\"current pen\",\"legend open?\",\"number of pens\"\n" +
    "\"0.0\",\"10.0\",\"0.0\",\"18.7\",\"true\",\"test pen\",\"false\",\"1\"\n" +
    "\n" +
    "\"pen name\",\"pen down?\",\"mode\",\"interval\",\"color\",\"x\"\n" +
    "\"test pen\",\"true\",\"0\",\"1.0\",\"0.0\",\"2.0\"\n" +
    "\n" +
    "\"test pen\"\n" +
    "\"x\",\"y\",\"color\",\"pen down?\"\n" +
    "\"0.0\",\"5.0\",\"0.0\",\"true\"\n" +
    "\"1.0\",\"8.0\",\"0.0\",\"true\"\n" +
    "\"2.0\",\"17.0\",\"0.0\",\"true\"\n"

  test("Simple Export") {
    val plot = new Plot("test plot")
    plot.pens = Nil
    // since it only has one pen, current pen will default to it.
    val pen = plot.createPlotPen("test pen",true)
    plot.plot(pen, 5)
    plot.plot(pen, 8)
    plot.plot(pen, 17)
    val export = exportPlotToString(plot)
    assert(EXPORT_RESULT === export.replaceAll("\r\n","\n"))
  }

  def exportPlotToString(plot:Plot) = {
    val stringWriter = new java.io.StringWriter
    val printWriter = new java.io.PrintWriter(stringWriter)
    val exporter = new PlotExporter(plot,csv)
    exporter.export(printWriter)
    printWriter.flush()
    stringWriter.toString
  }
}
