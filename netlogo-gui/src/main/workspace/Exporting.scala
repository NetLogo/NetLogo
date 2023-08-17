// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.AbstractExporter
import org.nlogo.api.{ Dump, ExportPlotWarningAction }
import org.nlogo.plot.CorePlotExporter
import java.io.{IOException,PrintWriter}

trait Exporting extends Plotting with ModelTracker { this: AbstractWorkspace =>

  def exportDrawingToCSV(writer:PrintWriter)
  def exportOutputAreaToCSV(writer:PrintWriter)

  def checkPlotUpdates() {
    if (!shouldUpdatePlots) {
      import ExportPlotWarningAction._
      exportPlotWarningAction match {
        case Throw => {
          setExportPlotWarningAction(ExportPlotWarningAction.Ignore)
          throw new Exception("Enable plot updating to use export-plot, export-all-plots, export-world, or export-interface.")
        }
        case Output => {
          setExportPlotWarningAction(ExportPlotWarningAction.Ignore)
          println("Enable plot updating to use export-plot, export-all-plots, export-world, or export-interface.")
        }
        case Ignore =>
      }
    }
  }

  @throws(classOf[IOException])
  def exportWorld(filename: String) {
    checkPlotUpdates
    new AbstractExporter(filename) {
      def export(writer: PrintWriter): Unit = {
        exportWorldNoMeta(writer)
      }
    }.export("world", modelFileName, "")
  }

  @throws(classOf[IOException])
  def exportWorld(writer: PrintWriter): Unit = {
    checkPlotUpdates
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
    checkPlotUpdates
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
    checkPlotUpdates
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
    checkPlotUpdates
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
