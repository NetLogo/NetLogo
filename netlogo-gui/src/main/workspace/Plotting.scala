// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.PlotInterface
import org.nlogo.plot.PlotManager
import org.nlogo.nvm.Context

trait Plotting { this: AbstractWorkspace =>

  val plotRNG = this.world.mainRNG.clone

  val realPlotManager = new PlotManager(this, plotRNG)
  val plotManager     = realPlotManager

  // methods used when importing plots
  def currentPlot(plot: String): Unit = {
    plotManager.currentPlot = plotManager.maybeGetPlot(plot)
  }

  def maybeGetPlot(plot: String): Option[PlotInterface] =
    plotManager.maybeGetPlot(plot)

  // The PlotManager has already-compiled thunks that it runs to setup and update
  // plots.  But those thunks need a Context to run in, which isn't known until
  // runtime.  So once we know the Context, we store it in a bit of mutable state
  // in Evaluator. - ST 3/2/10

  def updatePlots(c: Context): Unit = {
    evaluator.withContext(c){ plotManager.updatePlots() }
  }

  def setupPlots(c: Context): Unit = {
    evaluator.withContext(c){ plotManager.setupPlots() }
  }

}
