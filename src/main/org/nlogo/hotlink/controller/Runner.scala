package org.nlogo.hotlink.controller

import org.nlogo.api.SimpleJobOwner
import org.nlogo.agent.Observer
import org.nlogo.app.PlotTab
import org.nlogo.workspace.AbstractWorkspace
import java.io.File

class Runner(plotTab: PlotTab, workspace: AbstractWorkspace) extends Controller(plotTab, workspace) {

  val defaultOwner =
    new SimpleJobOwner("HotLink Runner", workspace.world.mainRNG,
                       classOf[Observer])

  @volatile var step: Boolean = false
  @volatile var notDone: Boolean = true

  def step(b:Boolean):Unit = {this.step = b}
  def end:Unit = { this.notDone = false; }

  //def tmpDir = System.getProperty("java.io.tmpdir") + "dthl/"

  override def run {
    val filesystemWorker = new File("tmp/" + plotTab.getNumberOfRuns)
    if (!filesystemWorker.isDirectory) {
      filesystemWorker.mkdir
      workspace.evaluateCommands(defaultOwner, "setup")
      if (!plotTab.getPlotPanel.alreadyPopulated()) {
        initializePlots
        //populateGraphPanel(plotTab.getNumberOfRuns , ticks);
      }
      record
    } else {
      if( step ) { tick }
    }

    if( !step ) { play }
  }

  def ticks = workspace.world.tickCounter.ticks.asInstanceOf[Int]
  def export() = workspace.exportView("tmp/" + plotTab.getNumberOfRuns + "/" + ticks, "png")

  def tick { this.workspace.evaluateCommands(defaultOwner, "go"); record; }

  def record: Unit = {
    export()
    plotTab.populateContentPanel
    populateGraphPanel(plotTab.getNumberOfRuns - 1 , ticks);
    plotTab.getViewPanel.goToLastFrame
  }

  def play {
    while (notDone) {
      tick
    }
  }
}
