// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

trait PlotListener {
  def clearAll()
  def clear()
  def defaultXMin(defaultXMin: Double)
  def defaultYMin(defaultYMin: Double)
  def defaultXMax(defaultXMax: Double)
  def defaultYMax(defaultYMax: Double)
  def defaultAutoPlotOn(defaultAutoPlotOn: Boolean)
  def autoPlotOn(flag: Boolean)
  def plotPenMode(plotPenMode: Int)
  def plot(x: Double, y: Double)
  def resetPen(hardReset: Boolean)
  def penDown(flag: Boolean)
  def setHistogramNumBars(num: Int)
  def currentPen(penName: String)
  def setPenColor(color: Int)
  def setInterval(interval: Double)
  def xRange(min: Double, max: Double)
  def yRange(min: Double, max: Double)
  def xMin(min: Double)
  def xMax(max: Double)
  def yMin(min: Double)
  def yMax(max: Double)
}
