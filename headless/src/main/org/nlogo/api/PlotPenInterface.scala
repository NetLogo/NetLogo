// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object PlotPenInterface {
  val MinMode = 0
  val MaxMode = 2
}

trait PlotPenInterface {
  def isDown_=(isDown: Boolean)
  def mode_=(mode: Int)
  def interval_=(interval: Double)
  def color_=(color: Int)
  def x_=(x: Double)
  def plot(x: Double, y: Double, color: Int, isDown: Boolean)
  def name: String
}
