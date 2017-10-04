// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.AbstractExporter
import org.nlogo.api.Dump
import org.nlogo.plot.PlotExporter
import java.io.{IOException,PrintWriter}

trait Exporting extends Plotting { this: AbstractWorkspace =>
  def extensionManager: ExtensionManager

  def exportDrawingToCSV(writer:PrintWriter)
  def exportOutputAreaToCSV(writer:PrintWriter)

  @throws(classOf[IOException])
  def exportWorld(filename: String) {
    new AbstractExporter(filename) {
      def export(writer:PrintWriter){
        world.exportWorld(writer,true)
        exportDrawingToCSV(writer)
        exportOutputAreaToCSV(writer)
        exportPlotsToCSV(writer)
        extensionManager.exportWorld(writer)
    } }.export("world",modelTracker.modelFileName,"")
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
    plotManager.getPlotNames.foreach { name =>
      new PlotExporter(plotManager.getPlot(name),Dump.csv).export(writer)
      writer.println()
    }
  }

  @throws(classOf[IOException])
  def exportPlot(plotName: String,filename: String) {
    new AbstractExporter(filename) {
      override def export(writer: PrintWriter) {
        exportInterfaceGlobals(writer)
        new PlotExporter(plotManager.getPlot(plotName),Dump.csv).export(writer)
      }
    }.export("plot",modelTracker.modelFileName,"")
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

        plotManager.getPlotNames.foreach { name =>
          new PlotExporter(plotManager.getPlot(name),Dump.csv).export(writer)
          writer.println()
        }
      }
    }.export("plots",modelTracker.modelFileName,"")
  }
}
