// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{List=>AWTList, _}
import event.{MouseAdapter, MouseEvent, MouseListener}
import javax.swing.border.Border
import org.nlogo.window.Events.{WidgetRemovedEvent, WidgetEditedEvent, WidgetAddedEvent}
import org.nlogo.api.{MultiErrorHandler, SingleErrorHandler, ModelSections},
  ModelSections.Saveable
import javax.swing.{JPanel, JMenuItem, JPopupMenu}

object Widget {
  trait LoadHelper {
    def version: String
    def convert(source: String, reporter: Boolean): String
  }
  val validWidgetTypes = List("BUTTON", "SLIDER", "SWITCH", "CHOOSER", "INPUT", "MONITOR", "PLOT", "NOTE")
  def validWidgetType(name:String) = validWidgetTypes.contains(name)
}

abstract class SingleErrorWidget extends Widget with SingleErrorHandler
abstract class MultiErrorWidget extends Widget with MultiErrorHandler

abstract class Widget extends JPanel with Saveable {

  import Widget.LoadHelper

  def helpLink: Option[String] = None
  var originalFont: Font = null
  var displayName: String = ""
  var deleteable: Boolean = true
  val widgetBorder: Border = org.nlogo.swing.Utils.createWidgetBorder
  val widgetPressedBorder: Border = org.nlogo.swing.Utils.createWidgetPressedBorder

  override def getPreferredSize: Dimension = getPreferredSize(getFont)
  def getPreferredSize(font: Font): Dimension = super.getPreferredSize
  def widgetWrapperOpaque = true
  def save: String
  def getEditable: Object = this
  def copyable = true // only OutputWidget and ViewWidget are not copyable
  def constrainDrag(newBounds: Rectangle, originalBounds: Rectangle, mouseMode: MouseMode): Rectangle = newBounds
  def isZoomed = if (findWidgetContainer != null) findWidgetContainer.isZoomed else false
  def load(strings: Array[String], helper: LoadHelper): Object
  def sourceOffset = 0
  def hasContextMenuInApplet = false
  def getUnzoomedPreferredSize: Dimension = getPreferredSize(originalFont)
  def needsPreferredWidthFudgeFactor = true
  def isButton = false
  def isTurtleForeverButton = false
  def isLinkForeverButton = false
  def isNote = false
  def hasContextMenu = false
  def exportable = false
  def zoomSubcomponents = false
  def getDefaultExportName = "output.txt"
  def export(exportPath: String): Unit = {}
  def updateConstraints(): Unit = {}
  def classDisplayName: String = getClass.getName
  def addExtraMenuItems(menu:JPopupMenu): Unit = {
    for(i<-extraMenuItems) menu.add(i)
  }
  def extraMenuItems: List[JMenuItem] = Nil

  def addPopupListeners(popupListener: MouseListener){ addPopupListeners(this, popupListener) }
  def addPopupListeners(): Unit = { addPopupListeners(this, popupListener) }
  private def addPopupListeners(component: Component, popupListener: MouseListener): Unit = {
    component.addMouseListener(popupListener)
    if (component.isInstanceOf[Container]) {
      var container: Container = component.asInstanceOf[Container]
      for(i<-0 until container.getComponentCount) addPopupListeners(container.getComponent(i), popupListener)
    }
  }

  def editFinished(): Boolean = {
    new WidgetEditedEvent(this).raise(this)
    true
  }

  protected def resetSizeInfo(): Unit = {
    if (findWidgetContainer != null) { findWidgetContainer.resetSizeInfo(this) }
  }

  private def doPopup(e: MouseEvent): Unit = {
    if (hasContextMenu) {
      var menu: JPopupMenu = new JPopupMenu
      populateContextMenu(menu, e.getPoint, e.getSource.asInstanceOf[Component])
      if (menu.getSubElements.length > 0) {
        menu.show(e.getSource.asInstanceOf[Component], e.getX, e.getY)
      }
      e.consume
    }
  }

  private final val popupListener: MouseListener = new MouseAdapter {
    override def mousePressed(e: MouseEvent){ if (e.isPopupTrigger) { doPopup(e) } }
    override def mouseReleased(e: MouseEvent){ if (e.isPopupTrigger) { doPopup(e) } }
  }

  def findWidgetContainer: WidgetContainer =
    org.nlogo.awt.Hierarchy.findAncestorOfClass(this, classOf[WidgetContainer])
      .orNull.asInstanceOf[WidgetContainer]

  def displayName(displayName: String): Unit = {
    this.displayName = displayName
    invalidate()
    repaint()
  }

  override def paintComponent(g: Graphics): Unit = {
    var g2d: Graphics2D = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
    super.paintComponent(g)
  }

  override def toString: String = {
    var sup: String = super.toString
    if (displayName != null && !displayName.equals("")) sup + "(" + displayName + ")"
    else sup
  }

  def populateContextMenu(menu: JPopupMenu, p: Point, source: Component): Point = p

  protected def resetZoomInfo(): Unit = {
    if (findWidgetContainer != null) {
      findWidgetContainer.resetZoomInfo(this)
    }
  }

  protected def getUnzoomedBounds: Rectangle = {
    if (findWidgetContainer != null) findWidgetContainer.getUnzoomedBounds(this)
    else getBounds
  }

  def getBoundsString: String = {
    if (findWidgetContainer != null) findWidgetContainer.getBoundsString(this)
    else {
      val buf: StringBuilder = new StringBuilder
      var r: Rectangle = getBounds
      buf.append(r.x + "\n")
      buf.append(r.y + "\n")
      buf.append((r.x + r.width) + "\n")
      buf.append((r.y + r.height) + "\n")
      buf.toString
    }
  }

  override def removeNotify: Unit = {
    if (java.awt.EventQueue.isDispatchThread) {
      org.nlogo.window.Event.rehash()
      new WidgetRemovedEvent(this).raise(this)
    }
    super.removeNotify()
  }

  override def addNotify: Unit = {
    super.addNotify
    if (originalFont == null) { originalFont = getFont }
    org.nlogo.window.Event.rehash()
    new WidgetAddedEvent(this).raise(this)
  }
}
