// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait PlotInterface {
  def name: String
  def getPen(pen: String): Option[PlotPenInterface]
  def currentPen_=(pen: String)
  def legendIsOpen_=(open: Boolean)
  var state: PlotState
  def plot(y: Double)
  def plot(x: Double, y: Double)
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
trait PlotPenInterface {
  def name: String
  var state: PlotPenState
}

case class PlotPenState(
  x: Double = 0.0,
  color: Int = java.awt.Color.BLACK.getRGB,
  interval: Double = 1.0,
  mode: Int = PlotPenInterface.LineMode,
  isDown: Boolean = true,
  hidden: Boolean = false
)

case class PlotState(
  autoPlotOn: Boolean = true,
  xMin: Double = 0,
  xMax: Double = 10,
  yMin: Double = 0,
  yMax: Double = 10
)
