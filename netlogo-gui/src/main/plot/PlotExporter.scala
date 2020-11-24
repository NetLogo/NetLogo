// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.CSV

@deprecated("Use `CorePlotExporter`", "6.1.2")
class PlotExporter(private val plot: Plot, private val csv: CSV)
  extends CorePlotExporter(plot, csv)
{

}
