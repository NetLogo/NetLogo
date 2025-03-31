// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.Options
import org.nlogo.plot.{ Plot, PlotManager }
import org.nlogo.window.Events.AfterLoadEvent

// the DummyPlotWidgetJava is only used when building a HubNet client interface
// all client plots are subordinate to a server plot thus, a plot on the client
// does not exist if there is no corresponding plot on the server.  rather than
// keep track of all the pens and such in two places we simply feed HubNet
// the pens from the server plot. ev 1/25/07
object DummyPlotWidget {
  def apply(name: String, plotManager: PlotManager): DummyPlotWidget = {
    new DummyPlotWidget(new Plot(name), plotManager)
  }
}

class DummyPlotWidget(plot: Plot, plotManager: PlotManager) extends AbstractPlotWidget(plot, plotManager) {
  var nameOptions = createNameOptions()

  override def editPanel: EditPanel = new DummyPlotEditPanel(this)

  override def load(model: WidgetModel): AnyRef = {
    super.load(model)
    nameOptions = createNameOptions()
    if (nameOptions.names.contains(plot.name)) {
      nameOptions.selectByName(plot.name)
    }
    this
  }

  override def handle(e: AfterLoadEvent): Unit = {}

  def setNameOptions(nameOptions: Options[Plot]): Unit = {
    this.nameOptions = nameOptions
    setPlotName(nameOptions.chosenName)
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
    val p: Plot = plotManager.maybeGetPlot(plot.name).getOrElse(throw new Exception("existing plot not found?"))
    for(pen <- p.pens){
      if (!pen.temporary) {
        s.append("\"" + org.nlogo.api.StringUtils.escapeString(pen.name) + "\" " +
                pen.defaultInterval + " " + pen.defaultMode + " " + pen.defaultColor + " " + pen.inLegend + "\n")
      }
    }
  }
}
