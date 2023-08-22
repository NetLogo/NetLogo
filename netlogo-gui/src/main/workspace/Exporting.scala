// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.AbstractExporter
import org.nlogo.api.{ Dump, ExportPlotWarningAction }
import org.nlogo.plot.CorePlotExporter
// import org.nlogo.swing.{ OptionDialog }
// import org.nlogo.core.I18N
import java.io.{ IOException,PrintWriter }

trait Exporting extends Plotting with ModelTracker { this: AbstractWorkspace =>

  def exportDrawingToCSV(writer:PrintWriter)
  def exportOutputAreaToCSV(writer:PrintWriter)

  def checkPlotUpdates() {
    import ExportPlotWarningAction._
    setTriedToExportPlot(true)
    exportPlotWarningAction match {
      case Output => {
        setExportPlotWarningAction(ExportPlotWarningAction.Ignore)
        println("Enable plot updating in Run Options to use export-plot, export-all-plots, export-world, or export-interface.")
      }
      case _ =>
    }
  }

  @throws(classOf[IOException])
  def exportWorld(filename: String) {
    if (!shouldUpdatePlots) {
      checkPlotUpdates
    }
    new AbstractExporter(filename) {
      def export(writer: PrintWriter): Unit = {
        exportWorldNoMeta(writer)
      }
    }.export("world", modelFileName, "")
  }

  @throws(classOf[IOException])
  def exportWorld(writer: PrintWriter): Unit = {
    if (!shouldUpdatePlots) {
      checkPlotUpdates
    }
    AbstractExporter.exportWithHeader(writer, "world", modelFileName, "")(exportWorldNoMeta _)
  }

  private def exportWorldNoMeta(writer: PrintWriter): Unit = {
    world.exportWorld(writer,true)
    exportDrawingToCSV(writer)
    exportOutputAreaToCSV(writer)
    exportPlotsToCSV(writer)
    extensionManager.exportWorld(writer)
  }

  def exportPlotsToCSV(writer: PrintWriter) = {
    if (!shouldUpdatePlots) {
      checkPlotUpdates
    }
    writer.println(Dump.csv.encode("PLOTS"))
    writer.println(
      Dump.csv.encode(
        plotManager.currentPlot.map(_.name).getOrElse("")))
    plotManager.getPlotNames.foreach { name =>
      new CorePlotExporter(
        plotManager
          .maybeGetPlot(name)
          .getOrElse(throw new Exception("plot manager gave a name for a plot that doesn't exist?"))
      , Dump.csv
      ).export(writer)
      writer.println()
    }
  }

  @throws(classOf[IOException])
  def exportPlot(plotName: String, filename: String) {
    if (!shouldUpdatePlots) {
      checkPlotUpdates
    }
    new AbstractExporter(filename) {
      override def export(writer: PrintWriter) {
        exportInterfaceGlobals(writer)
        new CorePlotExporter(
          plotManager
            .maybeGetPlot(plotName)
            .getOrElse(throw new Exception("plot with given name not found..."))
        , Dump.csv
        ).export(writer)
      }
    }.export("plot",modelFileName,"")
  }

  def exportInterfaceGlobals(writer: PrintWriter): Unit = {
    writer.println(Dump.csv.header("MODEL SETTINGS"))
    val globals = world.program.interfaceGlobals
    writer.println(Dump.csv.variableNameRow(globals))
    val values = globals.map(globalName => world.getObserverVariableByName(globalName))
    writer.println(Dump.csv.dataRow(values))
    writer.println()
  }

  @throws(classOf[IOException])
  def exportAllPlots(filename: String) {
    if (!shouldUpdatePlots) {
      checkPlotUpdates
    }
    new AbstractExporter(filename) {
    override def export(writer: PrintWriter) {
      exportInterfaceGlobals(writer)

      plotManager.getPlotNames.foreach { name =>
        new CorePlotExporter(
          plotManager
            .maybeGetPlot(name)
            .getOrElse(throw new Exception("plot manager gave a name for a plot that doesn't exist?"))
        , Dump.csv
        ).export(writer)
      }
    }
  }.export("plots",modelFileName,"")
  }
}
