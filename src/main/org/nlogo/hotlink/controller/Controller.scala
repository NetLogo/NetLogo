package org.nlogo.hotlink.controller

import org.nlogo.app.PlotTab
import org.nlogo.hotlink.graph.{HistoPlot, LinePlot, Plot}
import org.nlogo.workspace.AbstractWorkspace
import java.awt.Color
import org.nlogo.plot.{PlotPen, PlotManager}

abstract class Controller(val plotTab: PlotTab, val workspace: AbstractWorkspace) extends Runnable {

  def initializePlots(){
    updateGraphPanel(0)
    //populateGraphPanel(0,0)
  }

  def populateGraphPanel(run: Int , ticks: Int){
    val graphPanel = plotTab.getPlotPanel
    for ((netLogoPlot, plotCount) <- workspace.plotManager.asInstanceOf[PlotManager].plots.zipWithIndex) {

      //graphPanel.addNewTick(plotCount) // for histograms

      for ((pen, penCount) <- netLogoPlot.pens.zipWithIndex; if (pen.points.size > 0)) {
        // if it's a time series
        if (pen.mode == 0 || pen.mode == 2) { // line mode, point mode
            val point = pen.points.last
            if (graphPanel.getPlot(plotCount).getGraphType == classOf[LinePlot]) {
              graphPanel.addDataPoint(run, plotCount, penCount, ticks, point.y)
            }
        }
        if (pen.mode == 1) { // bar mode
          // for each tick, each pen has a whole histo.
          // so, we create an array of [series][category] = value
          //val data = new java.util.Array[java.lang.Double,java.lang.Double]()
          for (point <- pen.points) {
            if (graphPanel.getPlot(plotCount).getGraphType == classOf[HistoPlot]) {
              //print(point.x + " , " + point.y)
              //data.put(point.x , point.y)
              graphPanel.addDataPoint(run, plotCount, penCount, point.x, point.y)
            }
          }
          //graphPanel.addDataPoints(run, plotCount, penCount , data )
        }
      }
    }
  }

  def updateGraphPanel(run: Int) {
    //check plots. if new name, change. if new plot, add. add pens if needed.

    for( (netLogoPlot, plotCount) <- workspace.plotManager.asInstanceOf[PlotManager].plots.zipWithIndex ) {

      //System.out.println(netLogoPlot);

      // if we have a hotlink plot for the netlogo plot
      if( plotTab.getPlotPanel().getPlot(netLogoPlot) != null ) {
        // update the name in case it's been changed
        plotTab.getPlotPanel().getPlot(netLogoPlot).setName(netLogoPlot.getName);
      } else { // else make a new plot
        val plotType = if (netLogoPlot.pens.exists(pp => pp.mode == 1)) Plot.HISTOGRAM else Plot.LINE
        //plotTab.getPlotPanel.addPlot(netLogoPlot.name, plotType, netLogoPlot)
        plotTab.getPlotPanel().addPlot(netLogoPlot.name, plotType , netLogoPlot)
      }

      // update the pens. never take any away because we don't want to lose data,
       // just add the new ones if we need them.
      // add all the pens
      // only add the default pen if its the only pen...somewhat confusing.
      val goodPens = netLogoPlot.pens.filter(_.name!="default")
        for ( pen <- goodPens;
          if( plotTab.getPlotPanel.getPlot(netLogoPlot).getCollection().getPen( pen.name ) == null ) ) {
            plotTab.getPlotPanel.addPen(plotCount, pen.name, new Color( pen.color , true ))
        }
    }

    /*

    // for each plot (and its index)
    for ((plot, plotCount) <- workspace.plotManager.asInstanceOf[PlotManager].plots.zipWithIndex) {
      // add the plot to the plot tab panel thing, with the right type.
      val plotType = if (plot.pens.exists(pp => pp.mode == 1)) Plot.HISTOGRAM else Plot.LINE
      
      plotTab.getPlotPanel.addPlot(plot.name, plotType)

      // only add the default pen if its the only pen...somewhat confusing.
      for ( pen <- plot.pens;
           if ((pen.name != "default" && plot.pens.size > 1) || plot.pens.size == 1)) {
        plotTab.getPlotPanel.addPen(plotCount, pen.name, new Color(pen.color))
      }
    }
    plotTab.getPlotPanel.setAlreadyPopulated
    populateGraphPanel(0)

    */
  }
}
