// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Container, Dimension, Font, Graphics, Graphics2D, Point, Rectangle, event },
  event.{MouseAdapter, MouseEvent, MouseListener}
import javax.swing.border.Border
import javax.swing.{JPanel, JMenuItem, JPopupMenu}

import org.nlogo.api.{ MultiErrorHandler, SingleErrorHandler }
import org.nlogo.core.{ Widget => CoreWidget }
import org.nlogo.window.Events.{ WidgetAddedEvent, WidgetEditedEvent, WidgetRemovedEvent }

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

abstract class Widget extends JPanel {

  type WidgetModel <: CoreWidget

  def helpLink: Option[String] = None
  var originalFont: Font = null
  var displayName: String = ""
  var deleteable: Boolean = true
  val widgetBorder: Border = org.nlogo.swing.Utils.createWidgetBorder
  val widgetPressedBorder: Border = org.nlogo.swing.Utils.createWidgetPressedBorder

  override def getPreferredSize: Dimension = getPreferredSize(getFont)
  def getPreferredSize(font: Font): Dimension = super.getPreferredSize
  def widgetWrapperOpaque = true
  def getEditable: Object = this
  def copyable = true // only OutputWidget and ViewWidget are not copyable
  def constrainDrag(newBounds: Rectangle, originalBounds: Rectangle, mouseMode: MouseMode): Rectangle = newBounds
  def isZoomed = if (findWidgetContainer != null) findWidgetContainer.isZoomed else false

  def model: WidgetModel
  def reAdd(): Unit = { }
  def load(widget: WidgetModel): Object
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
      val container: Container = component.asInstanceOf[Container]
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
      val menu: JPopupMenu = new JPopupMenu
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
    val g2d: Graphics2D = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
    super.paintComponent(g)
  }

  override def toString: String = {
    val sup: String = super.toString
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
      val r: Rectangle = getBounds
      buf.append(r.x + "\n")
      buf.append(r.y + "\n")
      buf.append((r.x + r.width) + "\n")
      buf.append((r.y + r.height) + "\n")
      buf.toString
    }
  }

  def getBoundsTuple: (Int, Int, Int, Int) = {
    if (findWidgetContainer != null) findWidgetContainer.getBoundsTuple(this)
    else {
      val r: Rectangle = getBounds
      (r.x, r.y, r.x + r.width, r.y + r.height)
    }
  }

  override def removeNotify: Unit = {
    if (java.awt.EventQueue.isDispatchThread) {
      org.nlogo.window.Event.rehash()
      raiseWidgetRemoved()
    }
    super.removeNotify()
  }

  override def addNotify: Unit = {
    super.addNotify
    if (originalFont == null) { originalFont = getFont }
    org.nlogo.window.Event.rehash()
    raiseWidgetAdded()
  }

  // The methods to raise widget added/removed are here so they can be overridden by child classes.  Some of those
  // classes are not "actual" widgets they just use the UI functionality of this class, and changes to those items
  // (monitors, command lines, etc) should not cause things like marking the model as "dirty".
  // -Jeremy B November 2020
  def raiseWidgetRemoved(): Unit = {
    new WidgetRemovedEvent(this).raise(this)
  }
  def raiseWidgetAdded(): Unit = {
    new WidgetAddedEvent(this).raise(this)
  }

  implicit class RichStringOption(s: Option[String]) {
    def optionToPotentiallyEmptyString = s.getOrElse("")
  }

  implicit class RichWidgetString(s: String) {
    def potentiallyEmptyStringToOption = if (s != null && s.trim != "") Some(s) else None
  }
}
