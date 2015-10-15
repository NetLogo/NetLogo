// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait PlotPenInterface {
  def name: String
  var state: PlotPenState
}

object PlotPenInterface {
  val MinMode = 0
  val MaxMode = 2
  // These are integers, not an enum, because modelers actually use
  // these numbers to refer to the modes in their NetLogo yAxisCode.
  // (Why we didn't use strings, I'm not sure.) - ST 3/21/08
  val LineMode = 0
  val BarMode = 1
  val PointMode = 2
  def isValidPlotPenMode(mode: Int) = mode >= 0 && mode <= 2
}
