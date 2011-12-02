package org.nlogo.hotlink.controller

import org.nlogo.app.PlotTab
import org.nlogo.workspace.AbstractWorkspace

class Recorder(plotTab: PlotTab, workspace: AbstractWorkspace)
        extends Controller(plotTab, workspace) with TickListener{
  override def run {
    System.setProperty("org.nlogo.loggingEnabled", "true")
    // start listening to the logger for ticks
    org.nlogo.log.Logger.GLOBALS.addAppender(new TickListeningAppender(this))
  }

  def tick(ticks: Double) {
    //def tmpDir = System.getProperty("java.io.tmpdir") + "dthl/"
    if (ticks == 0.0) {
      if (!plotTab.getPlotPanel.alreadyPopulated) initializePlots()
      new java.io.File("tmp/" + plotTab.getNumberOfRuns).mkdir()
      populateGraphPanel(plotTab.getNumberOfRuns , 0)
    }
    workspace.exportView("tmp/" + plotTab.getNumberOfRuns + "/" + ticks.toInt, "png")
    populateGraphPanel(plotTab.getNumberOfRuns , 0)
  }
}