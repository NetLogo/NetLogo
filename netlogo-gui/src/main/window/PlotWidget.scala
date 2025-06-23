// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Point, Toolkit }
import java.awt.event.ActionEvent
import java.awt.image.BufferedImage
import javax.swing.{ AbstractAction, JMenuItem }

import org.nlogo.awt.ImageSelection
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.plot.{PlotManagerInterface, Plot}
import org.nlogo.swing.{ MenuItem, PopupMenu, Utils }
import org.nlogo.window.Events.PeriodicUpdateEvent

object PlotWidget{
  def apply(name: String, plotManager: PlotManagerInterface, colorizer: Colorizer): PlotWidget = {
    val plot = plotManager.newPlot(name)
    // create a default pen.
    plot.createPlotPen("default", false, "", "plot count turtles")
    // recompiling here is somewhat ugly.
    // we should probably ask the plotManager to create a new pen and it could recompile it.
    // this would save trouble in other places too where might forget to recompile.
    // however, this would take a good deal of work. maybe someday. -JC 6/1/10
    plotManager.compilePlot(plot)
    new PlotWidget(plot, plotManager, colorizer)
  }

  def apply(plotManager: PlotManagerInterface, colorizer: Colorizer): PlotWidget = this(plotManager.nextName, plotManager, colorizer)
}

class PlotWidget(plot: Plot, plotManager: PlotManagerInterface, colorizer: Colorizer)
  extends AbstractPlotWidget(plot, plotManager) with PeriodicUpdateEvent.Handler {

  override def editPanel: EditPanel = new PlotEditPanel(this, colorizer)

  override def getEditable: Option[Editable] = Some(this)

  def handle(e: PeriodicUpdateEvent): Unit ={ repaintIfNeeded() }

  override def hasContextMenu = true
  override def exportable = true
  override def getDefaultExportName = plotName + ".csv"
  override def reAdd(): Unit = {
    plotManager.addPlot(plot)
  }

  override def exportGraphics: BufferedImage = {
    val image = new BufferedImage(getWidth * 2, getHeight * 2, BufferedImage.TYPE_INT_ARGB)
    val g2d = Utils.initGraphics2D(image.createGraphics)

    g2d.scale(2, 2)

    paint(g2d)

    image
  }

  override def populateContextMenu(menu: PopupMenu, p: Point): Unit = {
    menu.add(new MenuItem(new AbstractAction(I18N.gui.get("edit.plot.copyimage")) {
      def actionPerformed(e: ActionEvent): Unit = {
        Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new ImageSelection(exportGraphics), null)
      }
    }))
  }

  override def extraMenuItems: List[JMenuItem] = List(
    new MenuItem(new AbstractAction(I18N.gui.get("edit.plot.clearplot")) {
      def actionPerformed(e: ActionEvent): Unit = {
        clear()
      }
    })
  )

  def repaintIfNeeded(): Unit ={
    canvas.repaintIfNeeded()
    refreshGUI()
    recolor()
  }
}
