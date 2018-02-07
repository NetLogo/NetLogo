// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Frame

import org.nlogo.core.I18N
import org.nlogo.plot.{ Plot, PlotManager }
import org.nlogo.swing.OptionDialog

import ExportControls._

class PlotExportControls(plotManager: PlotManager) {
  def plotNames = plotManager.getPlotNames

  def choosePlot(frame: Frame): Option[Plot] = {
    if (plotNames.length == 0) {
      sorryNoPlots(frame)
      None
    } else {
      val plotnum = OptionDialog.showAsList(frame,
        I18N.gui.get("menu.file.export.plot"),
        I18N.gui.get("menu.file.export.plot.whichPlot"),
        plotNames.toArray[Object])
      if (plotnum < 0)
        None
      else
        plotManager.getPlot(plotNames(plotnum))
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

  def sorryNoPlots(frame: Frame): Unit = {
    OptionDialog.showMessage(
      frame, I18N.gui.get("menu.file.export.plot"),
      I18N.gui.get("menu.file.export.noPlots"),
      Array[Object](I18N.gui.get("common.buttons.ok")))
  }
}
