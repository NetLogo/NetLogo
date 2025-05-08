// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.awt.{ Component, Dimension, Rectangle }
import java.awt.event.{ FocusListener, FocusEvent,
  KeyEvent, KeyAdapter, MouseAdapter, MouseEvent }
import java.awt.image.BufferedImage
import javax.swing.JLayeredPane

import org.nlogo.api.{ CompilerServices, Exceptions, RandomServices, Version }
import org.nlogo.awt.Images
import org.nlogo.core.{ Widget => CoreWidget, View => CoreView }
import org.nlogo.plot.PlotManager
import org.nlogo.swing.{ MenuItem, PopupMenu }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ LoadWidgetsEvent, OutputEvent }
import org.nlogo.util.SysInfo

import scala.collection.mutable.{ Map => MutableMap }

class InterfacePanelLite(val viewWidget: ViewWidgetInterface, compiler: CompilerServices,
  random: RandomServices, plotManager: PlotManager, editorFactory: EditorFactory)
  extends JLayeredPane
  with WidgetContainer
  with FocusListener
  with LoadWidgetsEvent.Handler
  with OutputEvent.Handler
  with ThemeSync {

  // widget name -> Widget
  private val widgets: MutableMap[String, Widget] = MutableMap[String, Widget]()

  // if sliderEventOnReleaseOnly is true, a SliderWidget will only raise an InterfaceGlobalEvent
  // when the mouse is released from the SliderDragControl
  // --mag 9/25/02, ST 4/9/03
  private var _sliderEventOnReleaseOnly: Boolean = false

  setOpaque(true)
  addFocusListener(this)
  addMouseListener(iPMouseListener)
  addKeyListener(getKeyAdapter)

  syncTheme()

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
        !e.isActionKey && (e.getModifiersEx & getToolkit.getMenuShortcutKeyMaskEx) == 0

    override def keyTyped(e: KeyEvent): Unit = {
      if (keyIsHandleable(e))
        Option(findActionButton(e.getKeyChar)).foreach(buttonKeyed)
    }

    def buttonKeyed(button: ButtonWidget): Unit = {
      button.keyTriggered()
    }
  }


  def focusGained(e: FocusEvent): Unit = {
    enableButtonKeys(true)
  }

  def focusLost(e: FocusEvent): Unit = {
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
    for (component <- getComponents) {
      val location = component.getLocation
      val size = component.getSize
      val x = location.x + size.width
      val y = location.y + size.height
      if (x > maxX)
        maxX = x
      if (y > maxY)
        maxY = y
    }
    new Dimension(maxX + 8, maxY + 8)
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

  def getUnzoomedBounds(component: Component): Rectangle =
    component.getBounds

  def resetZoomInfo(widget: Widget): Unit = { }

  def resetSizeInfo(widget: Widget): Unit = { }

  def isZoomed: Boolean = false

  ///

  private def doPopup(e: MouseEvent): Unit = {
    val menu = new PopupMenu

    menu.add(new MenuItem(Version.version)).setEnabled(false)
    menu.add(new MenuItem(SysInfo.getOSInfoString)).setEnabled(false)
    menu.add(new MenuItem(SysInfo.getVMInfoString)).setEnabled(false)
    menu.add(new MenuItem(SysInfo.getMemoryInfoString)).setEnabled(false)

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
    widget.syncTheme()
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

  private val widgetBuilderMap = Map[String, () => Widget](
    "Monitor"  -> (() => new MonitorWidget(random.auxRNG, compiler, editorFactory.colorizer)),
    "Plot"     -> (() => PlotWidget.apply(plotManager, editorFactory.colorizer)),
    "Slider"   -> (() => new SliderWidget(sliderEventOnReleaseOnly, random.auxRNG, compiler, editorFactory.colorizer)),
    "Chooser"  -> (() => new ChooserWidget(compiler, editorFactory.colorizer)),
    "InputBox" -> { () =>
      val singleLineConfig = editorFactory.defaultConfiguration(1, 10)
        .withFocusTraversalEnabled(true)
      val multiLineConfig = editorFactory.defaultConfiguration(5, 20)
      new InputBoxWidget(editorFactory.newEditor(singleLineConfig),
       editorFactory.newEditor(multiLineConfig), compiler, this)
    },
    "Button"   -> (() => new ButtonWidget(random.mainRNG, editorFactory.colorizer)),
    "Output"   -> (() => new OutputWidget()))

  override def allWidgets: Seq[CoreWidget] =
    widgets.map(_._2).map(_.model).toSeq.distinct

  def loadWidget(coreWidget: CoreWidget): Widget = {
    try {
      val x = coreWidget.x
      val y = coreWidget.y
      coreWidget match {
        case v: CoreView =>
          // the graphics widget (and the command center) are special cases because
          // they are not recreated at load time, but reused
          try {
            viewWidget.load(v)
          } catch {
            case ex: RuntimeException => Exceptions.handle(ex)
          }
          viewWidget.setSize(viewWidget.getSize())
          addWidget(viewWidget, x, y)
          viewWidget
        case _ =>
          val name = coreWidget.getClass.getSimpleName
          val newGuy = widgetBuilderMap.get(name).flatMap(createWidget =>
            try Some(createWidget())
            catch {
              case ex: RuntimeException =>
                Exceptions.handle(ex)
                None
            }).orElse(Option(WidgetRegistry(name)))

          newGuy.foreach { w =>
            w.load(coreWidget)
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

  def handle(e: LoadWidgetsEvent): Unit = {
    try {
      setVisible(false)
      e.widgets.foreach(loadWidget)
    } finally {
      setVisible(true)
      revalidate()
    }
  }

  def interfaceImage: BufferedImage =
    Images.paintToImage(this)

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.interfaceBackground())

    getComponents.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    })
  }
}
