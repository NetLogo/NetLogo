// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Action to take if PlotManager.compileAllPlot returns error(s)
 * during a call to open in HeadlessWorkspace.
 *
 * Throw  - Throw the first error
 * Output - Output all errors
 * Ignore - Do nothing
 */

abstract sealed trait PlotCompilationErrorAction

object PlotCompilationErrorAction {
  case object Throw extends PlotCompilationErrorAction
  case object Output extends PlotCompilationErrorAction
  case object Ignore extends PlotCompilationErrorAction
}

object PlotCompilationErrorActionJ {
  import PlotCompilationErrorAction._
  // convenience vals for Java access
  val THROW = Throw
  val OUTPUT = Output
  val IGNORE = Ignore
}
