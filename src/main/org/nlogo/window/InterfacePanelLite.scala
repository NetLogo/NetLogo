// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Dimension }
import java.awt.event.{ FocusEvent, FocusListener, KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }
import javax.swing.{ JLayeredPane, JMenuItem, JPopupMenu }
import org.nlogo.api.{ ParserServices, ModelReader, ModelSection, RandomServices, Version, VersionHistory }
import org.nlogo.plot.PlotManager
import org.nlogo.util.{ Exceptions, SysInfo }
import scala.collection.mutable

class InterfacePanelLite(val viewWidget: ViewWidgetInterface,
    parser: ParserServices, random: RandomServices,
    plotManager: PlotManager, editorFactory: EditorFactory) extends JLayeredPane with WidgetContainer
    with FocusListener with Events.LoadSectionEventHandler with Events.OutputEventHandler {
  private val widgets = new mutable.HashMap[String, Widget] // widget name -> Widget
  setOpaque(true)
  setBackground(Color.WHITE)
  addFocusListener(this)
  addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) =
        if(e.isPopupTrigger)
          doPopup(e)
        else
          requestFocus() // this is so the user can use action keys to control buttons - ST 8/31/04

      override def mouseReleased(e: MouseEvent) = if (e.isPopupTrigger) doPopup(e)
    })
  addKeyListener(getKeyAdapter)
  addWidget(viewWidget.asInstanceOf[Widget], 0, 0)

  def getKeyAdapter = new ButtonKeyAdapter

  class ButtonKeyAdapter extends KeyAdapter {
    protected def keyIsHandleable(e: KeyEvent) =
      e.getKeyChar != KeyEvent.CHAR_UNDEFINED &&
        !e.isActionKey &&
        (e.getModifiers & getToolkit.getMenuShortcutKeyMask) == 0

    override def keyTyped(e: KeyEvent) =
      if(keyIsHandleable(e)) {
        val button = findActionButton(e.getKeyChar)
        if(button != null)
          buttonKeyed(button)
      }

    def buttonKeyed(button: ButtonWidget) = button.keyTriggered
  }

  private var _hasFocus = true
  override def hasFocus = _hasFocus
  def hasFocus_=(b: Boolean) = _hasFocus = b
  def focusGained(e: FocusEvent) = {
    //println(s"iP focus gained from ${e.getOppositeComponent}")
    _hasFocus = true
    enableButtonKeys(true)
  }
  def focusLost(e: FocusEvent) = {
    //println(s"iP focus lost to ${e.getOppositeComponent}")
    _hasFocus = false
    enableButtonKeys(false)
  }
  override def requestFocus() = requestFocusInWindow()

  def reset() = getComponents foreach { comp => comp match {
      case plotWidget: PlotWidget =>
        plotManager.forgetPlot(plotWidget.plot)
      case _ if !comp.isInstanceOf[ViewWidgetInterface] =>
        remove(comp)
    }
  }

  private def enableButtonKeys(enabled: Boolean) = getComponents foreach { comp => comp match {
      case button: ButtonWidget => button.keyEnabled(enabled)
    }
  }

  protected def findActionButton(key: Char): ButtonWidget = {
    //println("findActionButton")
    getComponents foreach { comp => comp match {
        case button: ButtonWidget =>
          if (Character.toUpperCase(button.actionKey) == Character.toUpperCase(key))
            return button
      }
    }
    null
  }

  override def isOptimizedDrawingEnabled = false // our children may overlap

  override def getMinimumSize = new Dimension(0, 0)
  override def getPreferredSize = {
    var maxX, maxY = 0
    getComponents foreach { comp => if(comp.isInstanceOf[Widget]) {
        val location = comp.getLocation
        val size = comp.getSize
        val x = location.x + size.width
        val y = location.y + size.height
        if (x > maxX) maxX = x
        if (y > maxY) maxY = y
      }
    }
    new Dimension(maxX, maxY)
  }

  private def getOutputWidget: OutputWidget = {
    getComponents foreach { comp => comp match {
        case output: OutputWidget => return output
        case _ =>
      }
    }
    null
  }

  /// output

  def handle(e: Events.OutputEvent) = if(getOutputWidget != null && !e.toCommandCenter) {
      if(e.clear)
        getOutputWidget.outputArea.clear()
      if(e.outputObject != null)
        getOutputWidget.outputArea.append(e.outputObject, e.wrapLines)
    }

  ///

  def getBoundsString(widget: Widget) = {
    val r = getUnzoomedBounds(widget)
    s"${r.x}\n${r.y}\n${r.x+r.width}\n${r.y+r.height}\n"
  }
  def getUnzoomedBounds(component: Component) = component.getBounds
  def resetZoomInfo(widget: Widget) = {}
  def resetSizeInfo(widget: Widget) = {}
  def isZoomed = false

  ///

  private def doPopup(e: MouseEvent) = {
    val menu = new JPopupMenu
    var item = new JMenuItem(Version.version)
    item.setEnabled(false)
    menu.add(item)
    item = new JMenuItem(SysInfo.getOSInfoString)
    item.setEnabled(false)
    menu.add(item)
    item = new JMenuItem(SysInfo.getVMInfoString)
    item.setEnabled(false)
    menu.add(item)
    item = new JMenuItem(SysInfo.getMemoryInfoString)
    item.setEnabled(false)
    menu.add(item)
    menu.show(this, e.getX, e.getY)
  }

  ///

  private def addWidget(widget: Widget, x: Int, y: Int) = {
    // this is really no good in the long term, because widgets
    // don't have unique names. For now, who cares? - mmh
    widgets(widget.displayName) = widget
    widget.addPopupListeners()
    add(widget, JLayeredPane.DEFAULT_LAYER)
    moveToFront(widget)
    widget.setLocation(x, y)
    widget.validate()
  }

  def hideWidget(widgetName: String) = widgets.get(widgetName) foreach (_.setVisible(false))
  def showWidget(widgetName: String) = widgets.get(widgetName) foreach (_.setVisible(true))

  ///

  // if sliderEventOnReleaseOnly is true, a SliderWidget will only raise an InterfaceGlobalEvent
  // when the mouse is released from the SliderDragControl
  // --mag 9/25/02, ST 4/9/03
  var sliderEventOnReleaseOnly = false

  /// loading and saving

  def loadWidget(strings: Seq[String], modelVersion: String) = {
    val helper = new Widget.LoadHelper {
        def version = modelVersion
        def convert(source: String, reporter: Boolean) =
          parser.autoConvert(source, true, reporter, modelVersion)
      }
    try {
      val tpe = strings(0)
      var x = strings(1).toInt
      var y = strings(2).toInt
      if(tpe != "GRAPHICS-WINDOW" && VersionHistory.olderThan13pre1(modelVersion))
        y += viewWidget.getAdditionalHeight
      if(tpe == "GRAPHICS-WINDOW" || tpe == "VIEW") {
        // the graphics widget (and the command center) are special cases because
        // they are not recreated at load time, but reused
        try {
          viewWidget.asWidget.load(strings, helper)
        } catch {
          case ex: RuntimeException => Exceptions.handle(ex)
        }
        viewWidget.asWidget.setSize(viewWidget.asWidget.getSize)
        viewWidget.asWidget.setLocation(x, y)
        viewWidget.asWidget
      } else {
        var newGuy = WidgetRegistry(tpe)
        try {
          newGuy = tpe match {
            case "MONITOR" => new MonitorWidget(random.auxRNG)
            case "PLOT" => PlotWidget(plotManager)
            case "SLIDER" => new SliderWidget(sliderEventOnReleaseOnly, random.auxRNG)
            case "CHOOSER" | "CHOICE" => // new models use CHOOSER and old models use CHOICE
              new ChooserWidget(parser)
            case "INPUTBOX" =>
              new InputBoxWidget(editorFactory.newEditor(1, 20, false), editorFactory.newEditor(5, 20, true),
                parser, this)
            case "BUTTON" => new ButtonWidget(random.mainRNG)
            case "OUTPUT" => new OutputWidget
            case _ => WidgetRegistry(tpe)
          }
        } catch {
          case ex: RuntimeException => Exceptions.handle(ex)
        }
        if(newGuy != null) {
          newGuy.load(strings, helper)
          addWidget(newGuy, x, y)
        }
        newGuy
      }
    } catch {
      case ex: RuntimeException => Exceptions.handle(ex)
      null
    }
  }

  def handle(e: Events.LoadSectionEvent) = if(e.section == ModelSection.Interface) {
      try {
        val v = ModelReader.parseWidgets(e.lines)
        if(null != v) {
          setVisible(false)
          v foreach(loadWidget(_, e.version))
        }
      } finally {
        setVisible(true)
        revalidate()
      }
    }

  override def getWidgetsForSaving = {
    import scala.collection.JavaConverters._
    /* This is copied from WidgetPanel. Would be better to have a unified implementation
     * but there is currently no common superclass where to put it. I tried converting
     * WidgetContainer to a scala trait and have the implementation there, but I ran into
     * all sorts of trouble. Might give it another try eventually. NP 2012-09-13.
     */
    val result = new mutable.ArrayBuffer[Widget]
    // loop backwards so JLayeredPane gives us the components
    // in back-to-front order for saving - ST 9/29/03
    getComponents.reverse foreach { comp => comp match {
        case wrapper: WidgetWrapperInterface =>
          val widget = wrapper.widget
          if(!result.contains(widget)) result += widget
      }
    }
    result.asJava
  }
}
