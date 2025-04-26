// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.collection.immutable

trait PlotInterface {
  def name: String
  def pens: Seq[PlotPenInterface]
  def getPen(pen: String): Option[PlotPenInterface]
  def currentPen: Option[PlotPenInterface]
  def currentPenByName: String
  def currentPenByName_=(pen: String): Unit
  def legendIsOpen: Boolean
  def legendIsOpen_=(open: Boolean): Unit
  var state: PlotState
  def plot(y: Double): Unit
  def plot(x: Double, y: Double): Unit
  def histogramActions(pen: PlotPenInterface, values: Seq[Double]): immutable.Seq[PlotAction]
}
