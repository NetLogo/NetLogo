// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait PlotInterface {
  def xMin_=(xmin: Double)
  def xMax_=(xmax: Double)
  def yMin_=(ymin: Double)
  def yMax_=(ymax: Double)
  def autoPlotOn_=(autoPlot: Boolean)
  def legendIsOpen_=(open: Boolean)
  def currentPen_=(pen: String)
  def getPen(pen: String): Option[PlotPenInterface]
  def name: String
  def makeDirty()
}
