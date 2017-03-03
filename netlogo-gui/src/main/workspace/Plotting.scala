// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.{ MersenneTwisterFast, PlotInterface }
import org.nlogo.plot.PlotManager
import org.nlogo.nvm.Context

trait Plotting { this: AbstractWorkspace =>

  val plotRNG = new MersenneTwisterFast()

  val plotManager = new PlotManager(this, plotRNG)

  // methods used when importing plots
  def currentPlot(plot: String) {
    plotManager.currentPlot = Some(plotManager.getPlot(plot))
  }

  def getPlot(plot: String): PlotInterface = plotManager.getPlot(plot)

}
