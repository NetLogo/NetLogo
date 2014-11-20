// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.Options
import org.nlogo.plot.{Plot, PlotManager}
import org.nlogo.window.Events.AfterLoadEvent

// the DummyPlotWidgetJava is only used when building a HubNet client interface
// all client plots are subordinate to a server plot thus, a plot on the client
// does not exist if there is no corresponding plot on the server.  rather than
// keep track of all the pens and such in two places we simply feed HubNet
// the pens from the server plot. ev 1/25/07
object DummyPlotWidget{
  def apply(name: String, plotManager: PlotManager): DummyPlotWidget = {
    new DummyPlotWidget(new Plot(name), plotManager)
  }
}

class DummyPlotWidget(plot:Plot, plotManager: PlotManager) extends AbstractPlotWidget(plot, plotManager) {
  var nameOptions = createNameOptions()

  override def load(strings: Seq[String], helper: Widget.LoadHelper): Object = {
    super.load(strings, helper)
    nameOptions = createNameOptions()
    if (nameOptions.names.contains(plot.name)) {
      nameOptions.selectByName(plot.name)
    }
    this
  }

  override def handle(e: AfterLoadEvent): Unit = {}

  def nameOptions(nameOptions: Options[Plot]): Unit = {
    this.nameOptions = nameOptions
    plotName(nameOptions.chosenName)
  }

  def createNameOptions(): Options[Plot] = {
    val nameOptions = new Options[Plot]
    for((plot,i) <- plotManager.plots.zipWithIndex){
      nameOptions.addOption(plot.name, plot)
      if(i==0) nameOptions.selectValue(plot)
    }
    nameOptions
  }

  override def savePens(s: StringBuilder): Unit = {
    plotManager.getPlot(plot.name).foreach(p =>
      for(pen <- p.pens){
        if (!pen.temporary) {
          s.append("\"" + org.nlogo.api.StringUtils.escapeString(pen.name) + "\" " +
                  pen.defaultState.interval + " " + pen.defaultState.mode + " " + pen.defaultState.color + " " + pen.inLegend + "\n")
        }
      })
  }

  override def propertySet = {
    nameOptions = createNameOptions
    Properties.dummyPlot
  }
}
