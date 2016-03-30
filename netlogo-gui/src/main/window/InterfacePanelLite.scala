// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.{ Widget => CoreWidget }
import org.nlogo.core.model.WidgetReader
import org.nlogo.api.CompilerServices
import org.nlogo.api.ModelReader
import org.nlogo.api.ModelSection
import org.nlogo.api.RandomServices
import org.nlogo.api.Version
import org.nlogo.api.VersionHistory
import org.nlogo.plot.PlotManager
import org.nlogo.window.Events.{ LoadSectionEvent, OutputEvent }
import org.nlogo.fileformat
import org.nlogo.util.SysInfo
import org.nlogo.api.Exceptions

import javax.swing.JLayeredPane
import javax.swing.JPopupMenu
import javax.swing.JMenuItem
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.event.FocusListener
import java.awt.event.FocusEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyAdapter
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.{ List => JList }

import scala.collection.JavaConverters._
import scala.collection.mutable.{ Map => MutableMap }

class InterfacePanelLite(val viewWidget: ViewWidgetInterface, compiler: CompilerServices,
  random: RandomServices, plotManager: PlotManager, editorFactory: EditorFactory)
  extends JLayeredPane
  with WidgetContainer
  with FocusListener
  with LoadSectionEvent.Handler
  with OutputEvent.Handler {

  // widget name -> Widget
  private val widgets: MutableMap[String, Widget] = MutableMap[String, Widget]()
  private var _hasFocus: Boolean = true

  // if sliderEventOnReleaseOnly is true, a SliderWidget will only raise an InterfaceGlobalEvent
  // when the mouse is released from the SliderDragControl
  // --mag 9/25/02, ST 4/9/03
  private var _sliderEventOnReleaseOnly: Boolean = false

  setOpaque(true)
  setBackground(Color.WHITE)
  addFocusListener(this)
  addMouseListener(iPMouseListener)
  addKeyListener(getKeyAdapter)
  addWidget(viewWidget.asInstanceOf[Widget], 0, 0)

  // made protected so that hubnet could override it to implement message throttling. -JC 8/19/10
  protected def getKeyAdapter: KeyAdapter =
    new ButtonKeyAdapter()

  private lazy val iPMouseListener = new MouseAdapter() {
    override def mousePressed(e: MouseEvent): Unit = {
      if (e.isPopupTrigger)
        doPopup(e)
      else
        // this is so the user can use action keys to control buttons
        // - ST 8/31/04
        requestFocus()
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      if (e.isPopupTrigger)
        doPopup(e)
    }
  }

  // This is accessible ONLY FOR hubnet. Use it at your own peril. -- RG 6/11/15
  class ButtonKeyAdapter extends KeyAdapter {
    protected def keyIsHandleable(e: KeyEvent): Boolean =
      e.getKeyChar != KeyEvent.CHAR_UNDEFINED &&
        !e.isActionKey && (e.getModifiers & getToolkit.getMenuShortcutKeyMask) == 0

    override def keyTyped(e: KeyEvent): Unit = {
      if (keyIsHandleable(e))
        Option(findActionButton(e.getKeyChar)).foreach(buttonKeyed)
    }

    def buttonKeyed(button: ButtonWidget): Unit = {
      button.keyTriggered()
    }
  }


  def focusGained(e: FocusEvent): Unit = {
    _hasFocus = true
    enableButtonKeys(true)
  }

  def focusLost(e: FocusEvent): Unit = {
    _hasFocus = false
    enableButtonKeys(false)
  }

  override def requestFocus(): Unit = {
    requestFocusInWindow()
  }

  def reset(): Unit = {
    getComponents.foreach {
      case pw: PlotWidget           => plotManager.forgetPlot(pw.plot)
      case vwi: ViewWidgetInterface => remove(vwi)
      case _ =>
    }
  }

  private def enableButtonKeys(enabled: Boolean): Unit = {
    getComponents.foreach {
      case b: ButtonWidget => b.keyEnabled(enabled)
      case _ =>
    }
  }

  private[window] def findActionButton(key: Char): ButtonWidget = {
    import java.lang.Character.toUpperCase
    getComponents.collect {
      case b: ButtonWidget if toUpperCase(b.actionKey) == toUpperCase(key) => b
    }.headOption.orNull
  }

  override def isOptimizedDrawingEnabled: Boolean = false

  override def getMinimumSize: Dimension = new Dimension(0, 0)

  override def getPreferredSize: Dimension = {
    var maxX = 0
    var maxY = 0
    getComponents.foreach {
      case w: Widget =>
        val location = w.getLocation()
        val size = w.getSize()
        val x = location.x + size.width
        val y = location.y + size.height
        if (x > maxX)
          maxX = x
        if (y > maxY)
          maxY = y
      case _ =>
    }
    new Dimension(maxX, maxY)
  }

  private def getOutputWidget: OutputWidget =
    getComponents.collect {
      case ow: OutputWidget => ow
    }.headOption.orNull

  /// output

  def handle(e: OutputEvent): Unit =
    if (! e.toCommandCenter && getOutputWidget != null) {
      if (e.clear)
        getOutputWidget.outputArea.clear()
      if (e.outputObject != null)
        getOutputWidget.outputArea.append(e.outputObject, e.wrapLines)
    }

  ///

  def getBoundsString(widget: Widget): String = {
    val r = getUnzoomedBounds(widget)
    Seq(r.x, r.y, r.x + r.width, r.y + r.height).mkString("", "\n", "\n")
  }

  def getUnzoomedBounds(component: Component): Rectangle =
    component.getBounds

  def resetZoomInfo(widget: Widget): Unit = { }

  def resetSizeInfo(widget: Widget): Unit = { }

  def isZoomed: Boolean = false

  ///

  private def doPopup(e: MouseEvent): Unit = {
    val menu = new JPopupMenu()
    def disabledItem(s: String): JMenuItem = {
      val item = new javax.swing.JMenuItem(s)
      item.setEnabled(false)
      item
    }
    menu.add(disabledItem(Version.version))
    menu.add(disabledItem(SysInfo.getOSInfoString))
    menu.add(disabledItem(SysInfo.getVMInfoString))
    menu.add(disabledItem(SysInfo.getMemoryInfoString))
    menu.show(this, e.getX, e.getY)
  }

  ///

  private def addWidget(widget: Widget, x: Int, y: Int): Unit = {
    // this is really no good in the long term, because widgets
    // don't have unique names. For now, who cares? - mmh
    widgets += widget.displayName -> widget
    widget.addPopupListeners()
    add(widget, JLayeredPane.DEFAULT_LAYER)
    moveToFront(widget)
    widget.setLocation(x, y)
    widget.validate()
  }

  def hideWidget(widgetName: String): Unit = {
    widgets.get(widgetName).foreach(_.setVisible(false))
  }

  def showWidget(widgetName: String): Unit = {
    widgets.get(widgetName).foreach(_.setVisible(true))
  }

  ///

  def sliderEventOnReleaseOnly: Boolean =
    _sliderEventOnReleaseOnly

  def sliderEventOnReleaseOnly(sliderEventOnReleaseOnly: Boolean): Unit = {
    _sliderEventOnReleaseOnly = sliderEventOnReleaseOnly
  }

  /// loading and saving

  def loadWidget(strings: Array[String], coreWidget: CoreWidget, modelVersion: String): Widget = {
    val helper = new Widget.LoadHelper() {
      def version: String = modelVersion

      def convert(source: String, reporter: Boolean): String =
        compiler.autoConvert(source, true, reporter, modelVersion)
    }

    try {
      val widgetType = strings(0);
      val x = strings(1).toInt
      val y = strings(2).toInt
      if (widgetType == "GRAPHICS-WINDOW" || widgetType == "VIEW") {
        // the graphics widget (and the command center) are special cases because
        // they are not recreated at load time, but reused
        val widget = viewWidget.asWidget
        try {
          widget.load(coreWidget.asInstanceOf[widget.WidgetModel], helper)
        } catch {
          case ex: RuntimeException => Exceptions.handle(ex)
        }
        widget.setSize(viewWidget.asWidget.getSize)
        widget.setLocation(x, y)
        widget
      } else {
        val widgetMap = Map[String, () => Widget](
          "MONITOR" -> (() => new MonitorWidget(random.auxRNG)),
          "PLOT" -> (() => PlotWidget.apply(plotManager)),
          "SLIDER" -> (() => new SliderWidget(sliderEventOnReleaseOnly, random.auxRNG)),
          // for new models
          "CHOOSER" -> (() => new ChooserWidget(compiler)),
          // for old models
          "CHOICE" -> (() => new ChooserWidget(compiler)),
          "INPUTBOX" -> (() => new InputBoxWidget(
            editorFactory.newEditor(1, 20, false), editorFactory.newEditor(5, 20, true),
            compiler, this)),
          "BUTTON" -> (() => new ButtonWidget(random.mainRNG)),
          "OUTPUT" -> (() => new OutputWidget()))

        val newGuy = widgetMap.get(widgetType).flatMap(createWidget =>
          try Some(createWidget())
          catch {
            case ex: RuntimeException =>
              Exceptions.handle(ex)
              None
          })

        newGuy.foreach { w =>
          w.load(coreWidget.asInstanceOf[w.WidgetModel], helper)
          addWidget(w, x, y)
        }
        newGuy.orNull
      }
    } catch {
      case ex: RuntimeException =>
      Exceptions.handle(ex)
      null
    }
  }

  def handle(e: LoadSectionEvent): Unit =
    if (e.section == ModelSection.Interface) {
      try {
        val v: Seq[JList[String]] = ModelReader.parseWidgets(e.lines).asScala.toSeq
        if (null != v) {
          setVisible(false)
          // not sure we have access to workspace...
          val linesAndWidgets = v zip v.map(lines =>
              WidgetReader.read(lines.asScala.toList, compiler, fileformat.hubNetReaders))
          linesAndWidgets.foreach {
            case (lines, coreWidget) => loadWidget(lines.toArray(new Array[String](lines.size)), coreWidget, e.version)
          }
        }
      } finally {
        setVisible(true)
        revalidate()
      }
    }
}
