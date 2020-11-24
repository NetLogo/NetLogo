// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.collection.immutable

trait PlotInterface {
  def name: String
  def pens: Seq[PlotPenInterface]
  def getPen(pen: String): Option[PlotPenInterface]
  def currentPen: Option[PlotPenInterface]
  def currentPenByName: String
  def currentPenByName_=(pen: String)
  def legendIsOpen: Boolean
  def legendIsOpen_=(open: Boolean)
  var state: PlotState
  def plot(y: Double)
  def plot(x: Double, y: Double)
  def histogramActions(pen: PlotPenInterface, values: Seq[Double]): immutable.Seq[PlotAction]
}
