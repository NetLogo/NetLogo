// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.PlotPenState

trait PlotPenInterface {
  def name: String
  def state: PlotPenState
  def state_=(s: PlotPenState): Unit
  def plot(x: Double, y: Double, color: Int, isDown: Boolean): Unit
  def points: Seq[PlotPointInterface]
}
