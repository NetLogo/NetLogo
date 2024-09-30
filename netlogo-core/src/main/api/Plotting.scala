// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait PlotManagerInterface {
  def nextName: String
  def publish(action: PlotAction)
  def currentPlot: Option[PlotInterface]
  def setCurrentPlot(name: String)
  def hasPlot(name: String): Boolean
  def getPlotNames: Seq[String]
  // `maybeGetPlot()` to avoid conflicting with the concrete `PlotManager` implementations' `getPlot()` methods.
  // We cannot override those as their `Plot` implementations differ too much from `PlotInterface`.  Someday they
  // should be reconciled and unified into a single `org.nlogo.plot` package in `netlogo-core`, but not today.
  // -Jeremy B Octover 2020
  def maybeGetPlot(name: String): Option[PlotInterface]
}

case class PlotState(
  autoPlotX: Boolean = true,
  autoPlotY: Boolean = true,
  xMin: Double = 0,
  xMax: Double = 10,
  yMin: Double = 0,
  yMax: Double = 10
)
