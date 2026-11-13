// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Frame

import org.nlogo.core.LocaleContext
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
      val plotnum = new DropdownOptionPane(frame, loc"Export Plot", loc"Which plot would you like to export?",
                                           plotNames).getChoiceIndex
      if (plotnum == -1)
        None
      else
        plotManager.maybeGetPlot(plotNames(plotnum))
    }
  }

  def allPlotExportFailed(frame: Frame, filename: String, ex: Exception): Unit = {
    displayExportError(frame, loc"Export of all plots to ${filename} failed: ${ex.getMessage}", loc"Export Plot Failed")
  }

  def singlePlotExportFailed(frame: Frame, filename: String, plot: Plot, ex: Exception): Unit = {
    displayExportError(frame, loc"Export of ${plot.name} plot to ${filename} failed: ${ex.getMessage}",
                       loc"Export Plot Failed")
  }

  def sorryNoPlots(frame: Frame): Unit = {
    new OptionPane(frame, loc"Export Plot", loc"There are no plots to export.",
                   OptionPane.Options.Ok, OptionPane.Icons.info)
  }
}
