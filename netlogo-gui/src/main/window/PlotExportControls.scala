// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Frame

import org.nlogo.core.I18N
import org.nlogo.plot.{ Plot, PlotManager }
import org.nlogo.swing.{ DropdownOptionPane, OptionPane }

import ExportControls._

class PlotExportControls(plotManager: PlotManager) {
  def plotNames = plotManager.getPlotNames

  def choosePlot(frame: Frame): Option[Plot] = {
    if (plotNames.length == 0) {
      sorryNoPlots(frame)
      None
    } else {
      val plotnum = new DropdownOptionPane(frame, I18N.gui.get("menu.file.export.plot"),
                                           I18N.gui.get("menu.file.export.plot.whichPlot"),
                                           plotNames).getChoiceIndex
      if (plotnum == -1)
        None
      else
        plotManager.maybeGetPlot(plotNames(plotnum))
    }
  }

  def allPlotExportFailed(frame: Frame, filename: String, ex: Exception): Unit = {
    displayExportError(
      frame, I18N.gui.getN("menu.file.export.allPlots.exportFailed", filename, ex.getMessage),
      I18N.gui.get("menu.file.export.plot.failed"))
  }

  def singlePlotExportFailed(frame: Frame, filename: String, plot: Plot, ex: Exception): Unit = {
    displayExportError(
      frame, I18N.gui.getN("menu.file.export.plot.exportFailed", plot.name, filename, ex.getMessage),
      I18N.gui.get("menu.file.export.plot.failed"))
  }

  def sorryNoPlots(frame: Frame) {
    new OptionPane(frame, I18N.gui.get("menu.file.export.plot"), I18N.gui.get("menu.file.export.noPlots"),
                   OptionPane.Options.Ok, OptionPane.Icons.Info)
  }
}
