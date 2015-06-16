// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object PlotPenInterface {
  val MinMode = 0
  val MaxMode = 2
}

trait PlotPenInterface {
  def isDown_=(isDown: Boolean): Unit
  def mode_=(mode: Int): Unit
  def interval_=(interval: Double): Unit
  def color_=(color: Int): Unit
  def x_=(x: Double): Unit
  def plot(x: Double, y: Double, color: Int, isDown: Boolean): Unit
  def name: String
}
