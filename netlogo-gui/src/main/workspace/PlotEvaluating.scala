// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.nvm.Context

trait PlotEvaluating { this: AbstractWorkspace with Plotting with Evaluating =>

  // The PlotManager has already-compiled thunks that it runs to setup and update
  // plots.  But those thunks need a Context to run in, which isn't known until
  // runtime.  So once we know the Context, we store it in a bit of mutable state
  // in Evaluator. - ST 3/2/10
  def updatePlots(c: Context) {
    evaluator.withContext(c){ plotManager.updatePlots() }
  }

  def setupPlots(c: Context) {
    evaluator.withContext(c){ plotManager.setupPlots() }
  }
}
