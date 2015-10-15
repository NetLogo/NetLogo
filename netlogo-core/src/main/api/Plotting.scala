// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.collection.immutable

trait PlotManagerInterface {
  def nextName: String
  def publish(action: PlotAction)
  def currentPlot: Option[PlotInterface]
  def setCurrentPlot(name: String)
  def hasPlot(name: String): Boolean
  def getPlotNames: Seq[String]
}

case class PlotState(
  autoPlotOn: Boolean = true,
  xMin: Double = 0,
  xMax: Double = 10,
  yMin: Double = 0,
  yMax: Double = 10
)
