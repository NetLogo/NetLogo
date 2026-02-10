// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Container, Dimension, Font, Graphics, Point, Rectangle, event },
                event.{ MouseAdapter, MouseEvent, MouseListener }
import javax.swing.{ JPanel, JMenuItem }

import org.nlogo.api.CompilerServices
import org.nlogo.core.{ NetLogoPreferences, TokenType, Widget => CoreWidget }
import org.nlogo.swing.{ PopupMenu, RoundedBorderPanel }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Event
import org.nlogo.window.Events.{ InterfaceModeChangedEvent, WidgetAddedEvent, WidgetErrorEvent, WidgetRemovedEvent }

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

    new WidgetErrorEvent(this, Option(e)).raise(this)
  }

  override def error(key: Object, e: Exception): Unit = {
    super.error(key, e)

    new WidgetErrorEvent(this, Option(e)).raise(this)
  }
}

abstract class MultiErrorWidget extends Widget with MultiErrorHandler {
  override def removeAllErrors(): Unit = {
    super.removeAllErrors()

    new WidgetErrorEvent(this, None).raise(this)
  }

  override def error(key: Object, e: Exception): Unit = {
    super.error(key, e)

    new WidgetErrorEvent(this, Option(e)).raise(this)
  }
}

abstract class Widget extends JPanel with RoundedBorderPanel with ThemeSync with InterfaceModeChangedEvent.Handler {
  def helpLink: Option[String] = None
  var originalFont: Font = null
  var displayName: String = ""
  var deleteable: Boolean = true

  setBorderColor(InterfaceColors.Transparent)

  private var zoomFactor = 1.0

  protected var _oldSize = false
  protected var _boldState = {
    if (NetLogoPreferences.getBoolean("boldWidgetText", false)) {
      Font.BOLD
    } else {
      Font.PLAIN
    }
  }

  protected var widgetContainer: Option[WidgetContainer] = None

  def getWidgetContainer: Option[WidgetContainer] =
    widgetContainer

  def setWidgetContainer(container: WidgetContainer): Unit = {
    widgetContainer = Option(container)
  }

  def getEditable: Option[Editable]
  def copyable = true // only OutputWidget and ViewWidget are not copyable
  def constrainDrag(newBounds: Rectangle, originalBounds: Rectangle, mouseMode: MouseMode): Rectangle = newBounds
  def isZoomed: Boolean = {
    // this couldn't possibly happen ...right?
    // well somehow it does happen when loading monitor widgets, so here we are (Isaac B 6/24/25)
    widgetContainer != null && widgetContainer.exists(_.isZoomed)
  }

  def model: CoreWidget
  def reAdd(): Unit = { }
  def load(widget: CoreWidget): Unit
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
    initGUI()
    revalidate()
    repaint()
  }
  def getZoomFactor: Double =
    zoomFactor
  def zoom(d: Double): Int =
    (zoomFactor * d).toInt
  def oldSize: Boolean =
    _oldSize
  def oldSize(value: Boolean): Unit = {
    _oldSize = value
    initGUI()
    revalidate()
    repaint()
  }
  def setBoldText(value: Boolean): Unit = {
    if (value) {
      _boldState = Font.BOLD
    } else {
      _boldState = Font.PLAIN
    }
    initGUI()
    revalidate()
    repaint()
  }
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
    true
  }

  // this method is for widgets that need to redo their layout when a visual property changes (Isaac B 3/1/25)
  def initGUI(): Unit = {}

  protected def resetSizeInfo(): Unit = {
    widgetContainer.foreach(_.resetSizeInfo(this))
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
    widgetContainer.foreach(_.resetZoomInfo(this))
  }

  def getUnzoomedBounds: Rectangle =
    widgetContainer.map(_.getUnzoomedBounds(this)).getOrElse(getBounds)

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
  }

  // The methods to raise widget added/removed are here so they can be overridden by child classes.  Some of those
  // classes are not "actual" widgets they just use the UI functionality of this class, and changes to those items
  // (monitors, command lines, etc) should not cause things like marking the model as "dirty".
  // -Jeremy B November 2020
  def raiseWidgetRemoved(): Unit = {
    new WidgetRemovedEvent(this).raise(this)
  }
  def raiseWidgetAdded(): Unit = {
    Event.rehash()

    widgetContainer.foreach(new WidgetAddedEvent(this).raise(_))
  }

  def handle(e: InterfaceModeChangedEvent): Unit = {
    resetMouseState()
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
