// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.plot.Plot

sealed trait PlotWidgetExport

object PlotWidgetExport {
  case class ExportSinglePlot(plot: Plot) extends PlotWidgetExport
  case object ExportAllPlots extends PlotWidgetExport
}
