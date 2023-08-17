// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

abstract sealed trait ExportPlotWarningAction

object ExportPlotWarningAction {
  case object Throw extends ExportPlotWarningAction
  case object Output extends ExportPlotWarningAction
  case object Ignore extends ExportPlotWarningAction
}

object ExportPlotWarningActionJ {
  import ExportPlotWarningAction._
  // convenience vals for Java access
  val THROW = Throw
  val OUTPUT = Output
  val IGNORE = Ignore
}
