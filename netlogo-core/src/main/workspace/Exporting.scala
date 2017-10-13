// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.AbstractExporter
import org.nlogo.api.{ Dump, ThreeDVersion, TwoDVersion, Version }
import org.nlogo.plot.PlotExporter
import java.io.{IOException,PrintWriter}

trait Exporting extends Plotting { this: AbstractWorkspace =>
  def extensionManager: ExtensionManager

  def exportDrawingToCSV(writer:PrintWriter)
  def exportOutputAreaToCSV(writer:PrintWriter)

  protected def exportVersion: Version =
    if (dialect.is3D) ThreeDVersion
    else              TwoDVersion

  @throws(classOf[IOException])
  def exportWorld(filename: String) {
    new AbstractExporter(filename) {
      def export(writer:PrintWriter){
        world.exportWorld(writer,true)
        exportDrawingToCSV(writer)
        exportOutputAreaToCSV(writer)
        exportPlotsToCSV(writer)
        extensionManager.exportWorld(writer)
    } }.export("world",modelTracker.modelFileName, exportVersion, "")
  }

  def exportWorld(writer:PrintWriter) {
    world.exportWorld(writer,true)
    exportDrawingToCSV(writer)
    exportOutputAreaToCSV(writer)
    exportPlotsToCSV(writer)
    extensionManager.exportWorld(writer)
  }

  def exportPlotsToCSV(writer: PrintWriter) = {
    writer.println(Dump.csv.encode("PLOTS"))
    writer.println(
      Dump.csv.encode(
        plotManager.currentPlot.map(_.name).getOrElse("")))
    for {
      name <- plotManager.getPlotNames
      plot <- plotManager.getPlot(name)
    } {
      new PlotExporter(plot, Dump.csv).export(writer)
      writer.println()
    }
  }

  @throws(classOf[IOException])
  def exportPlot(plotName: String,filename: String) {
    new AbstractExporter(filename) {
      override def export(writer: PrintWriter) {
        exportInterfaceGlobals(writer)
        for {
          plot <- plotManager.getPlot(plotName)
        } {
          new PlotExporter(plot, Dump.csv).export(writer)
        }
      }
    }.export("plot",modelTracker.modelFileName, exportVersion, "")
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
    new AbstractExporter(filename) {
      override def export(writer: PrintWriter) {
        exportInterfaceGlobals(writer)
        for {
          name <- plotManager.getPlotNames
          plot <- plotManager.getPlot(name)
        } {
          new PlotExporter(plot, Dump.csv).export(writer)
          writer.println()
        }
      }
    }.export("plots",modelTracker.modelFileName, exportVersion, "")
  }
}
