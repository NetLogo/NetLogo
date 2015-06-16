// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait PlotInterface {
  def xMin_=(xmin: Double): Unit
  def xMax_=(xmax: Double): Unit
  def yMin_=(ymin: Double): Unit
  def yMax_=(ymax: Double): Unit
  def autoPlotOn_=(autoPlot: Boolean): Unit
  def legendIsOpen_=(open: Boolean): Unit
  def currentPen_=(pen: String): Unit
  def getPen(pen: String): Option[PlotPenInterface]
  def name: String
  def makeDirty(): Unit
}
