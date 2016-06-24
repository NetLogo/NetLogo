// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{JMenuItem, JPopupMenu}
import org.nlogo.awt.ImageSelection
import org.nlogo.window.Events.{ExportPlotEvent, PeriodicUpdateEvent}
import java.awt.{Component, Point}
import java.awt.image.BufferedImage
import org.nlogo.swing.RichJMenuItem
import org.nlogo.plot.{PlotManagerInterface, Plot}

object PlotWidget{
  def apply(name:String, plotManager:PlotManagerInterface): PlotWidget = {
    val plot = plotManager.newPlot(name)
    // create a default pen.
    plot.createPlotPen("default", false, "", "plot count turtles")
    // recompiling here is somewhat ugly.
    // we should probably ask the plotManager to create a new pen and it could recompile it.
    // this would save trouble in other places too where might forget to recompile.
    // however, this would take a good deal of work. maybe someday. -JC 6/1/10
    plotManager.compilePlot(plot)
    new PlotWidget(plot, plotManager)
  }

  def apply(plotManager: PlotManagerInterface): PlotWidget = this(plotManager.nextName, plotManager)
}

class PlotWidget(plot:Plot, plotManager: PlotManagerInterface) extends AbstractPlotWidget(plot, plotManager)
        with PeriodicUpdateEvent.Handler {

  def handle(e: PeriodicUpdateEvent){ repaintIfNeeded() }

  override def hasContextMenu = true
  override def exportable = true
  override def getDefaultExportName = plotName + ".csv"

  override def exportGraphics: BufferedImage = {
    val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    paint(image.getGraphics)
    image
  }

  override def export(exportPath: String): Unit = {
    new ExportPlotEvent(PlotWidgetExportType.ARGUMENT, plot, exportPath).raise(this)
  }

  override def populateContextMenu(menu: JPopupMenu, p: Point, source: Component): Point = {
    val copyItem = RichJMenuItem("Copy Image"){
      java.awt.Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new ImageSelection(exportGraphics), null)
    }
    menu.add(copyItem)
    p
  }

  override def extraMenuItems: List[JMenuItem] = List(
    RichJMenuItem("Clear"){ clear() }
  )

  def repaintIfNeeded(){
    canvas.repaintIfNeeded()
    refreshGUI()
    recolor()
  }
}
