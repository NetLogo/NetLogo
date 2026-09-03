// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Container, Dimension, Font, Graphics, Insets, Point, Rectangle, event },
                event.{ MouseAdapter, MouseEvent, MouseListener }
import javax.swing.{ JPanel, JMenuItem }
import javax.swing.border.Border

import org.nlogo.api.CompilerServices
import org.nlogo.core.{ NetLogoPreferences, TokenType, Widget => CoreWidget }
import org.nlogo.swing.{ PopupMenu, PreferredSize, RoundedBorderPanel, Utils, Zoomable }
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

abstract class Widget extends JPanel with RoundedBorderPanel with Zoomable with ThemeSync
                      with InterfaceModeChangedEvent.Handler {

  def helpLink: Option[(String, String)] = None
  var displayName: String = ""
  var deleteable: Boolean = true

  setBorderColor(InterfaceColors.Transparent)
  setDiameter(12)

  protected var _oldSize = false
  protected var _boldState = {
    if (NetLogoPreferences.getBoolean("boldWidgetText", false)) {
      Font.BOLD
    } else {
      Font.PLAIN
    }
  }

  protected var unzoomedBounds: Rectangle = new Rectangle(0, 0, 0, 0)

  protected var widgetContainer: Option[WidgetContainer] = None

  def getWidgetContainer: Option[WidgetContainer] =
    widgetContainer

  def setWidgetContainer(container: WidgetContainer): Unit = {
    widgetContainer = Option(container)
  }

  def getEditable: Option[Editable]
  def copyable = true // only OutputWidget and ViewWidget are not copyable
  def constrainDrag(newBounds: Rectangle, originalBounds: Rectangle, mouseMode: MouseMode): Rectangle = newBounds

  def model: CoreWidget
  def reAdd(): Unit = { }
  def load(widget: CoreWidget): Unit
  def sourceOffset = 0
  def hasContextMenuInApplet = false
  def isButton = false
  def isTurtleForeverButton = false
  def isLinkForeverButton = false
  def isNote = false
  def hasContextMenu = false
  def exportable = false
  def oldSize: Boolean =
    _oldSize
  def oldSize(value: Boolean): Unit = {
    _oldSize = value
    revalidate()
    repaint()
  }
  def setBoldText(value: Boolean): Unit = {
    if (value) {
      _boldState = Font.BOLD
    } else {
      _boldState = Font.PLAIN
    }
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

  override def toString: String = {
    val sup: String = super.toString
    if (displayName != null && !displayName.equals("")) {
      sup + "(" + displayName + ")"
    } else {
      sup
    }
  }

  def populateContextMenu(menu: PopupMenu, p: Point): Unit = {}

  def getUnzoomedBounds: Rectangle =
    unzoomedBounds

  def setUnzoomedBounds(bounds: Rectangle): Unit = {
    unzoomedBounds = bounds
  }

  def setUnzoomedBounds(x: Int, y: Int, width: Int, height: Int): Unit = {
    unzoomedBounds = new Rectangle(x, y, width, height)
  }

  override def removeNotify: Unit = {
    if (java.awt.EventQueue.isDispatchThread) {
      org.nlogo.window.Event.rehash()
      raiseWidgetRemoved()
    }
    super.removeNotify()
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

  def setCodeFont(font: Font): Unit = {}

  protected class AdaptableHorizontalStrut(oldSize: Int, newSize: Int) extends Component with PreferredSize {
    override def getMinimumSize: Dimension = {
      if (_oldSize) {
        new Dimension(Utils.zoom(oldSize), 0)
      } else {
        new Dimension(Utils.zoom(newSize), 0)
      }
    }

    override def getPreferredSize: Dimension =
      getMinimumSize

    override def getMaximumSize: Dimension =
      new Dimension(getMinimumSize.width, Int.MaxValue)
  }

  protected class AdaptableVerticalStrut(oldSize: Int, newSize: Int) extends Component with PreferredSize {
    override def getMinimumSize: Dimension = {
      if (_oldSize) {
        new Dimension(0, Utils.zoom(oldSize))
      } else {
        new Dimension(0, Utils.zoom(newSize))
      }
    }

    override def getPreferredSize: Dimension =
      getMinimumSize

    override def getMaximumSize: Dimension =
      new Dimension(Int.MaxValue, getMinimumSize.height)
  }

  protected class AdaptableBorder(oldInsets: Insets, newInsets: Insets) extends Border {
    override def getBorderInsets(component: Component): Insets = {
      if (_oldSize) {
        Utils.zoomInsets(oldInsets)
      } else {
        Utils.zoomInsets(newInsets)
      }
    }

    override def isBorderOpaque: Boolean =
      false

    override def paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int): Unit = {}
  }

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
