// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

trait PlotListener {
  def clearAll(): Unit
  def clear(): Unit
  def defaultXMin(defaultXMin: Double): Unit
  def defaultYMin(defaultYMin: Double): Unit
  def defaultXMax(defaultXMax: Double): Unit
  def defaultYMax(defaultYMax: Double): Unit
  def defaultAutoPlotX(defaultAutoPlotX: Boolean): Unit
  def defaultAutoPlotY(defaultAutoPlotY: Boolean): Unit
  def autoPlotX(flag: Boolean): Unit
  def autoPlotY(flag: Boolean): Unit
  def plotPenMode(plotPenMode: Int): Unit
  def plot(x: Double, y: Double): Unit
  def resetPen(hardReset: Boolean): Unit
  def penDown(flag: Boolean): Unit
  def setHistogramNumBars(num: Int): Unit
  def currentPen(penName: String): Unit
  def setPenColor(color: Int): Unit
  def setInterval(interval: Double): Unit
  def xRange(min: Double, max: Double): Unit
  def yRange(min: Double, max: Double): Unit
  def xMin(min: Double): Unit
  def xMax(max: Double): Unit
  def yMin(min: Double): Unit
  def yMax(max: Double): Unit
}
