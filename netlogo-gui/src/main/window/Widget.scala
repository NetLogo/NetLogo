// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Container, Dimension, Font, Graphics, Point, Rectangle, event },
                event.{ MouseAdapter, MouseEvent, MouseListener }
import java.util.prefs.Preferences
import javax.swing.{ JPanel, JMenuItem }

import org.nlogo.api.{ CompilerServices, MultiErrorHandler, SingleErrorHandler }
import org.nlogo.core.{ TokenType, Widget => CoreWidget }
import org.nlogo.swing.{ PopupMenu, RoundedBorderPanel }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ WidgetAddedEvent, WidgetEditedEvent, WidgetErrorEvent, WidgetRemovedEvent }

object Widget {
  trait LoadHelper {
    def version: String
    def convert(source: String, reporter: Boolean): String
  }
  val validWidgetTypes = List("BUTTON", "SLIDER", "SWITCH", "CHOOSER", "INPUT", "MONITOR", "PLOT", "NOTE")
  def validWidgetType(name: String) = validWidgetTypes.contains(name)
}

abstract class SingleErrorWidget extends Widget with SingleErrorHandler {
  override def error(e: Exception): Unit = {
    super.error(e)

    new WidgetErrorEvent(this, e).raise(this)
  }

  override def error(key: Object, e: Exception): Unit = {
    super.error(key, e)

    new WidgetErrorEvent(this, e).raise(this)
  }
}

abstract class MultiErrorWidget extends Widget with MultiErrorHandler {
  override def removeAllErrors(): Unit = {
    super.removeAllErrors()

    new WidgetErrorEvent(this, null).raise(this)
  }

  override def error(key: Object, e: Exception): Unit = {
    super.error(key, e)

    new WidgetErrorEvent(this, e).raise(this)
  }
}

abstract class Widget extends JPanel with RoundedBorderPanel with ThemeSync {

  type WidgetModel <: CoreWidget

  def helpLink: Option[String] = None
  var originalFont: Font = null
  var displayName: String = ""
  var deleteable: Boolean = true

  setBorderColor(InterfaceColors.Transparent)

  protected val preserveWidgetSizes = Preferences.userRoot.node("/org/nlogo/NetLogo")
                                                 .getBoolean("preserveWidgetSizes", true)

  protected var zoomFactor = 1.0

  def getEditable: Object = this
  def copyable = true // only OutputWidget and ViewWidget are not copyable
  def constrainDrag(newBounds: Rectangle, originalBounds: Rectangle, mouseMode: MouseMode): Rectangle = newBounds
  def isZoomed: Boolean = if (findWidgetContainer != null) findWidgetContainer.isZoomed else false

  def model: WidgetModel
  def reAdd(): Unit = { }
  def load(widget: WidgetModel): Object
  def sourceOffset = 0
  def hasContextMenuInApplet = false
  def getUnzoomedPreferredSize: Dimension = getPreferredSize
  def isButton = false
  def isTurtleForeverButton = false
  def isLinkForeverButton = false
  def isNote = false
  def hasContextMenu = false
  def exportable = false
  def setZoomFactor(zoomFactor: Double): Unit = {
    this.zoomFactor = zoomFactor
  }
  def getZoomFactor: Double =
    zoomFactor
  def getDefaultExportName = "output.txt"
  def updateConstraints(): Unit = {}
  def classDisplayName: String = getClass.getName
  def addExtraMenuItems(menu: PopupMenu): Unit = {
    extraMenuItems.foreach(menu.add)
  }
  def extraMenuItems: List[JMenuItem] = Nil

  def addPopupListeners(popupListener: MouseListener): Unit = { addPopupListeners(this, popupListener) }
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
      val menu = new PopupMenu

      populateContextMenu(menu, e.getPoint)

      if (menu.getSubElements.length > 0)
        menu.show(e.getSource.asInstanceOf[Component], e.getX, e.getY)

      e.consume
    }
  }

  private final val popupListener: MouseListener = new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = { if (e.isPopupTrigger) { doPopup(e) } }
    override def mouseReleased(e: MouseEvent): Unit = { if (e.isPopupTrigger) { doPopup(e) } }
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
    setDiameter(12 * zoomFactor)

    super.paintComponent(g)
  }

  override def toString: String = {
    val sup: String = super.toString
    if (displayName != null && !displayName.equals("")) {
      sup + "(" + displayName + ")"
    } else {
      sup
    }
  }

  def populateContextMenu(menu: PopupMenu, p: Point): Unit = {}

  protected def resetZoomInfo(): Unit = {
    if (findWidgetContainer != null)
      findWidgetContainer.resetZoomInfo(this)
  }

  def getUnzoomedBounds: Rectangle = {
    if (findWidgetContainer != null) {
      findWidgetContainer.getUnzoomedBounds(this)
    } else {
      getBounds
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

  protected def checkRecursive(compiler: CompilerServices, source: String, name: String): Boolean =
    compiler.tokenizeForColorization(source).exists(token => token.tpe == TokenType.Ident && token.text == name)

  implicit class RichStringOption(s: Option[String]) {
    def optionToPotentiallyEmptyString = s.getOrElse("")
  }

  implicit class RichWidgetString(s: String) {
    def potentiallyEmptyStringToOption = {
      if (s != null && s.trim != "") {
        Some(s)
      } else {
        None
      }
    }
  }
}
