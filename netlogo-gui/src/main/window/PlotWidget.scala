// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{JMenuItem, JPopupMenu}
import org.nlogo.awt.ImageSelection
import org.nlogo.window.Events.PeriodicUpdateEvent
import java.awt.{Component, Point}
import java.awt.image.BufferedImage
import org.nlogo.swing.RichJMenuItem
import org.nlogo.plot.{PlotManagerInterface, Plot}
import org.nlogo.core.I18N

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
  override def reAdd(): Unit = {
    plotManager.addPlot(plot)
  }

  override def exportGraphics: BufferedImage = {
    val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    paint(image.getGraphics)
    image
  }

  override def populateContextMenu(menu: JPopupMenu, p: Point, source: Component): Point = {
    val copyItem = RichJMenuItem(I18N.gui.get("edit.plot.copyimage")){
      java.awt.Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new ImageSelection(exportGraphics), null)
    }
    menu.add(copyItem)
    p
  }

  override def extraMenuItems: List[JMenuItem] = List(
    RichJMenuItem(I18N.gui.get("edit.plot.clearplot")){ clear() }
  )

  def repaintIfNeeded(){
    canvas.repaintIfNeeded()
    refreshGUI()
    recolor()
  }
}
