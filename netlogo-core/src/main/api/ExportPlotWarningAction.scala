// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

abstract sealed trait ExportPlotWarningAction

object ExportPlotWarningAction {
  case object Warn extends ExportPlotWarningAction
  case object Output extends ExportPlotWarningAction
  case object Ignore extends ExportPlotWarningAction
}

object ExportPlotWarningActionJ {
  import ExportPlotWarningAction._
  // convenience vals for Java access
  val WARN = Warn
  val OUTPUT = Output
  val IGNORE = Ignore
}
