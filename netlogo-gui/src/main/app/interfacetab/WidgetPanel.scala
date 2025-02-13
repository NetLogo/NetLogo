// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Component, Cursor, Dimension, Graphics, MouseInfo, Point, Rectangle, Color => AwtColor, Toolkit }
import java.awt.event.{ ActionEvent, KeyAdapter, KeyEvent, KeyListener, MouseAdapter, MouseEvent, MouseListener,
                        MouseMotionAdapter, MouseMotionListener }
import java.awt.image.BufferedImage
import javax.swing.{ AbstractAction, JComponent, JLayeredPane, SwingUtilities }

import org.nlogo.api.Editable
import org.nlogo.app.common.EditorFactory
import org.nlogo.app.common.Events.InterfaceModeEvent
import org.nlogo.awt.{ Fonts => NlogoFonts, Mouse => NlogoMouse }
import org.nlogo.core.{ I18N, Button => CoreButton, Chooser => CoreChooser, InputBox => CoreInputBox,
  Monitor => CoreMonitor, Plot => CorePlot, Slider => CoreSlider, Switch => CoreSwitch, TextBox => CoreTextBox,
  View => CoreView, Widget => CoreWidget }
import org.nlogo.editor.{ EditorArea, EditorConfiguration }
import org.nlogo.log.LogManager
import org.nlogo.nvm.DefaultCompilerServices
import org.nlogo.swing.{ MenuItem, PopupMenu, Utils }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ AbstractWidgetPanel, ButtonWidget, Events => WindowEvents, GUIWorkspace, OutputWidget,
                          Widget, WidgetContainer, WidgetRegistry, DummyChooserWidget, DummyInputBoxWidget,
                          DummyPlotWidget, DummyViewWidget, PlotWidget },
  WindowEvents.{ CompileAllEvent, DirtyEvent, EditWidgetEvent, LoadBeginEvent, WidgetEditedEvent, WidgetRemovedEvent,
                 ZoomedEvent }

sealed trait InteractMode {
  def cursor: Cursor =
    Cursor.getDefaultCursor
}

object InteractMode {
  case object INTERACT extends InteractMode
  case object SELECT extends InteractMode
  case object ADD extends InteractMode {
    override def cursor =
      Toolkit.getDefaultToolkit.createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), null)
  }

  case object EDIT extends InteractMode {
    override def cursor =
      Toolkit.getDefaultToolkit.createCustomCursor(Utils.iconScaledWithColor("/images/edit-cursor.png", 32, 32,
                                                                             InterfaceColors.toolbarImage).getImage,
                                                   new Point(0, 16), I18N.gui.get("tabs.run.widget.editWidget"))
  }

  case object DELETE extends InteractMode
}

// note that an instance of this class is used for the hubnet client editor
// and its subclass InterfacePanel is used for the interface tab.
// there are a few things in here that are specific to the client editor behavior
// (eg the way it handles plots) which is overridden in the subclass. ev 1/25/07

object WidgetPanel {
  val GridSnap = 5 // grid size, in pixels
}

// public for widget extension - ST 6/12/08
class WidgetPanel(val workspace: GUIWorkspace)
    extends AbstractWidgetPanel
    with WidgetContainer
    with MouseListener
    with MouseMotionListener
    with KeyListener
    with WidgetEditedEvent.Handler
    with WidgetRemovedEvent.Handler
    with LoadBeginEvent.Handler {

  import WidgetPanel.GridSnap

  protected var selectionRect: Rectangle = null // convert to Option?
  var widgetsBeingDragged: Seq[WidgetWrapper] = Seq()
  private var view: Widget = null // convert to Option?

  // if sliderEventOnReleaseOnly is true, a SliderWidget will only raise an InterfaceGlobalEvent
  // when the mouse is released from the SliderDragControl
  // --mag 9/25/02, ST 4/9/03
  var sliderEventOnReleaseOnly: Boolean = false

  protected var startDragPoint: Option[Point] = None
  protected var newWidget: Option[WidgetWrapper] = None
  protected var selectionPane: JComponent =
    new JComponent() {
      override def paintComponent(g: Graphics): Unit = {
        if (selectionRect != null) {
          g.setColor(AwtColor.WHITE)
          g.drawRect(selectionRect.x, selectionRect.y,
            selectionRect.width - 1, selectionRect.height - 1)
          g.setColor(new AwtColor(180, 180, 180, 120))
          g.fillRect(selectionRect.x, selectionRect.y,
            selectionRect.width - 1, selectionRect.height - 1)
        }
      }
    }

  protected class InterceptPane extends JComponent {
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        WidgetPanel.this.mousePressed(e)
      }

      override def mouseReleased(e: MouseEvent): Unit = {
        WidgetPanel.this.mouseReleased(e)
      }
    })

    addMouseMotionListener(new MouseMotionAdapter {
      override def mouseMoved(e: MouseEvent): Unit = {
        WidgetPanel.this.mouseMoved(e)
      }

      override def mouseDragged(e: MouseEvent): Unit = {
        WidgetPanel.this.mouseDragged(e)
      }
    })

    addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = {
        WidgetPanel.this.keyPressed(e)
      }

      override def keyReleased(e: KeyEvent): Unit = {
        WidgetPanel.this.keyReleased(e)
      }
    })

    def enableIntercept(): Unit = {
      setBounds(0, 0, WidgetPanel.this.getWidth - 10, WidgetPanel.this.getHeight - 10)
    }

    def disableIntercept(): Unit = {
      setBounds(0, 0, 0, 0)
    }
  }

  protected var interceptPane = new InterceptPane

  add(interceptPane, JLayeredPane.DRAG_LAYER)

  protected val editorFactory: EditorFactory = new EditorFactory(workspace, workspace.getExtensionManager)

  protected var interactMode: InteractMode = InteractMode.INTERACT

  def getInteractMode: InteractMode =
    interactMode

  def setInteractMode(mode: InteractMode): Unit = {
    if (interactMode == InteractMode.ADD && !placedShadowWidget)
      removeShadowWidget()

    interactMode = mode

    if (mode == InteractMode.INTERACT) {
      interceptPane.disableIntercept()

      getWrappers.foreach(_.disableShadow())
    } else {
      interceptPane.enableIntercept()

      if (getWrappers.exists(_.widget match {
        case b: ButtonWidget if b.running => true
        case _ => false
      })) {
        workspace.halt()
      }

      getWrappers.foreach(_.enableShadow())
    }

    if (mode == InteractMode.EDIT || mode == InteractMode.INTERACT || mode == InteractMode.DELETE)
      unselectWidgets()

    setCursor(mode.cursor)

    requestFocus()

    new InterfaceModeEvent(mode).raise(this)
  }

  private var placedShadowWidget = false
  private var stampWidget = false

  setOpaque(true)
  setBackground(AwtColor.WHITE)
  setAutoscrolls(true)
  selectionPane.setOpaque(false)
  selectionPane.setVisible(false)

  add(selectionPane, JLayeredPane.DRAG_LAYER)

  addMouseListener(this)
  addMouseMotionListener(this)
  addKeyListener(this)

  // our children may overlap
  override def isOptimizedDrawingEnabled: Boolean = false

  override def requestFocus(): Unit = {
    requestFocusInWindow()
  }

  override def getMinimumSize: Dimension =
    new Dimension(0, 0)

  override def getPreferredSize: Dimension = {
    var maxX = 0
    var maxY = 0
    for { component <- getComponents if component ne selectionPane } {
      val location = component.getLocation
      val size = component.getSize
      var x = location.x + size.width
      var y = location.y + size.height
      if (component.isInstanceOf[WidgetWrapper] &&
        ! component.asInstanceOf[WidgetWrapper].selected) {
        x += WidgetWrapper.BORDER_E
        y += WidgetWrapper.BORDER_S
      }
      if (x > maxX)
        maxX = x
      if (y > maxY)
        maxY = y
    }
    // allow for the intrusion of the window grow box into the
    // lower right corner
    maxX += 8
    maxY += 8
    new Dimension(maxX, maxY)
  }

  private def getWrappers: Seq[WidgetWrapper] = {
    getComponents.collect {
      case ww: WidgetWrapper => ww
    }
  }

  private def wrapperAtPoint(point: Point): Option[WidgetWrapper] =
    getWrappers.filter(ww => ww.getSelectedBounds.contains(point)).sortBy(getPosition(_)).headOption

  override def empty: Boolean =
    getComponents.exists {
      case w: WidgetWrapper => true
      case _ => false
    }

  ///

  private[app] def getOutputWidget: OutputWidget = {
    getComponents.collect {
      case w: WidgetWrapper if w.widget.isInstanceOf[OutputWidget] =>
        w.widget.asInstanceOf[OutputWidget]
    }.headOption.orNull
  }

  ///

  def getWrapper(widget: Widget): WidgetWrapper =
    widget.getParent.asInstanceOf[WidgetWrapper]

  def selectedWrappers: Seq[WidgetWrapper] =
    getComponents.collect {
      case w: WidgetWrapper if w.selected => w
    }

  private def unselectedWrappers: Seq[WidgetWrapper] =
    getComponents.collect {
      case w: WidgetWrapper if !w.selected => w
    }

  private[interfacetab] def aboutToDragSelectedWidgets(startPressX: Int, startPressY: Int): Unit = {
    widgetsBeingDragged = selectedWrappers
    widgetsBeingDragged.foreach { w =>
      w.aboutToDrag(startPressX, startPressY)
      moveToFront(w)
    }
  }

  private[interfacetab] def dragSelectedWidgets(x: Int, y: Int): Unit = {
    if (widgetsBeingDragged.nonEmpty) {
      val p = new Point(x, y)
      val restrictedPoint = widgetsBeingDragged.foldLeft(p) {
        case (p, w) => restrictDrag(p, w)
      }
      widgetsBeingDragged.foreach { w => w.doDrag(restrictedPoint.x, restrictedPoint.y) }
    }
  }

  protected def restrictDrag(p: Point, w: WidgetWrapper): Point = {
    var x = p.x
    var y = p.y
    val wb = w.originalBounds
    val b = getBounds()
    val newWb = new Rectangle(wb.x + x, wb.y + y, wb.width, wb.height)
    if (workspace.snapOn && ! isZoomed) {
      val xGridSnap = newWb.x - (newWb.x / GridSnap) * GridSnap
      val yGridSnap = newWb.y - (newWb.y / GridSnap) * GridSnap
      x -= xGridSnap
      y -= yGridSnap
      newWb.x -= xGridSnap
      newWb.y -= yGridSnap
    }

    if (newWb.x < 0)
      x += - newWb.x
    if (newWb.y < WidgetWrapper.BORDER_N)
      y += WidgetWrapper.BORDER_N - newWb.y
    if (newWb.x + 2 * WidgetWrapper.BORDER_W > b.width)
      x -= (newWb.x + 2 * WidgetWrapper.BORDER_W) - b.width
    if (newWb.y + WidgetWrapper.BORDER_N > b.height)
      y -= (newWb.y + WidgetWrapper.BORDER_N) - b.height

    new Point(x, y)
  }

  def dropSelectedWidgets(): Unit = {
    widgetsBeingDragged.foreach(_.doDrop())
    widgetsBeingDragged = Seq()
    setForegroundWrapper()
  }

  def mouseMoved(e: MouseEvent): Unit = {
    interactMode match {
      case InteractMode.ADD =>
        newWidget.foreach(widget => {
          if (workspace.snapOn) {
            widget.setLocation((e.getX / GridSnap) * GridSnap, (e.getY / GridSnap) * GridSnap)
          } else {
            widget.setLocation(e.getX, e.getY)
          }

          widget.originalBounds = widget.getBounds
        })

      case InteractMode.EDIT =>
        val topWrapper = wrapperAtPoint(e.getPoint).getOrElse(null)

        getWrappers.foreach(wrapper => selectWidget(wrapper, wrapper == topWrapper))

      case InteractMode.DELETE =>
        val topWrapper = wrapperAtPoint(e.getPoint).filter(_.widget.deleteable).getOrElse(null)

        getWrappers.foreach(wrapper => selectWidget(wrapper, wrapper == topWrapper))

      case _ =>
    }
  }

  def mouseDragged(e: MouseEvent): Unit = {
    if (widgetsBeingDragged.nonEmpty) {
      wrapperAtPoint(e.getPoint).foreach(w => {
        e.translatePoint(-w.getX, -w.getY)
        w.mouseDragged(e)
      })
    } else if (NlogoMouse.hasButton1(e)) {
      startDragPoint match {
        case Some(point) =>
          val dx = e.getX - point.x
          val dy = e.getY - point.y

          // this check reduces the likelihood that a click is registered as a drag (Isaac B 1/30/25)
          if (dx * dx + dy * dy < 25)
            return

          interactMode match {
            case InteractMode.INTERACT =>
              if (NlogoMouse.hasCtrl(e))
                setInteractMode(InteractMode.SELECT)

            case InteractMode.SELECT =>
              if (!selectionPane.isVisible) {
                selectionPane.setBounds(0, 0, getWidth, getHeight)
                selectionPane.setVisible(true)
              }
              scrollRectToVisible(new Rectangle(e.getX - 20, e.getY - 20, 40, 40))
              val oldSelectionRect = selectionRect
              val x = StrictMath.min(getWidth, StrictMath.max(e.getX, 0))
              val y = StrictMath.min(getHeight, StrictMath.max(e.getY, 0))
              selectionRect =
                new java.awt.Rectangle(
                  StrictMath.min(point.x, x),
                  StrictMath.min(point.y, y),
                  StrictMath.abs(x - point.x),
                  StrictMath.abs(y - point.y)
                )
              selectWidgets(selectionRect, NlogoMouse.hasCtrl(e))
              if (oldSelectionRect != null)
                selectionPane.repaint(oldSelectionRect)
              selectionPane.repaint(selectionRect)

            case InteractMode.ADD =>
              if (workspace.snapOn) {
                point.x = (point.x / GridSnap) * GridSnap
                point.y = (point.y / GridSnap) * GridSnap
              }

              newWidget.foreach(widget => {
                val p2 = restrictDrag(new Point(e.getX - point.x, e.getY - point.y), widget)

                widget.setLocation(point.x + p2.x, point.y + p2.y)
              })

            case InteractMode.EDIT =>
              val topWrapper = wrapperAtPoint(e.getPoint).getOrElse(null)

              getWrappers.foreach(wrapper => selectWidget(wrapper, wrapper == topWrapper))

            case InteractMode.DELETE =>
              val topWrapper = wrapperAtPoint(e.getPoint).filter(_.widget.deleteable).getOrElse(null)

              getWrappers.foreach(wrapper => selectWidget(wrapper, wrapper == topWrapper))
          }

        case None =>
          wrapperAtPoint(e.getPoint).foreach(w => {
            e.translatePoint(-w.getX, -w.getY)
            w.mouseDragged(e)
          })
      }
    }
  }

  def mouseEntered(e: MouseEvent): Unit = { }
  def mouseExited(e: MouseEvent): Unit = { }
  def mouseClicked(e: MouseEvent): Unit = { }

  def mousePressed(e: MouseEvent): Unit = {
    interactMode match {
      case InteractMode.SELECT =>
        if (e.getButton == MouseEvent.BUTTON1) {
          requestFocus()

          if (NlogoMouse.hasCtrl(e)) {
            startDragPoint = Some(e.getPoint)
          } else {
            wrapperAtPoint(e.getPoint) match {
              case Some(w) if w.selected =>
                e.translatePoint(-w.getX, -w.getY)
                w.mousePressed(e)
                startDragPoint = None

              case _ =>
                startDragPoint = Some(e.getPoint)
            }
          }
        } else {
          wrapperAtPoint(e.getPoint) match {
            case Some(w) =>
              e.translatePoint(-w.getX, -w.getY)
              w.mouseReleased(e)

            case _ =>
              if (e.isPopupTrigger)
                doPopup(e.getPoint)
          }
        }

      case InteractMode.ADD =>
        if (e.isPopupTrigger)
          doPopup(e.getPoint)

      case _ =>
        if (e.getButton == MouseEvent.BUTTON1) {
          // this is so the user can use action keys to control buttons
          // - ST 8/6/04,8/31/04
          requestFocus()
          startDragPoint = Some(e.getPoint)
        } else {
          wrapperAtPoint(e.getPoint) match {
            case Some(w) =>
              e.translatePoint(-w.getX, -w.getY)
              w.mouseReleased(e)

            case _ =>
              if (e.isPopupTrigger)
                doPopup(e.getPoint)
          }
        }
    }
  }

  // this is bordering on comical its so confusing.
  // this method runs for the hubnet client editor.
  // im not yet sure if it runs anywhere else.
  // that seems like bugs waiting to happen. JC - 12/20/10
  protected def doPopup(point: Point): Unit = {
    val menu = new PopupMenu

    def menuItem(keyName: String, widget: CoreWidget): WidgetCreationMenuItem = {
      new WidgetCreationMenuItem(I18N.gui.get(s"tabs.run.widgets.$keyName"), widget)
    }
    val plot = menuItem("plot", CorePlot(None))
    val menuItems = Seq(
      menuItem("button",  CoreButton(None, 0, 0, 0, 0)),
      menuItem("slider",  CoreSlider(None)),
      menuItem("switch",  CoreSwitch(None)),
      menuItem("chooser", CoreChooser(None)),
      menuItem("input",   CoreInputBox(None)),
      menuItem("monitor", CoreMonitor(None, 0, 0, 0, 0, None, 10)),
      plot,
      menuItem("note", CoreTextBox(None, fontSize = 11, color = 0)))
    menuItems.foreach(menu.add)

    // if there are no plots in this model, then you can't have a plot in a hubnet client.
    if (workspace.plotManager.plots.size == 0)
      plot.setEnabled(false)

    menu.show(this, point.x, point.y)
  }

  protected class WidgetCreationMenuItem(displayName: String, coreWidget: CoreWidget)
    extends MenuItem(new AbstractAction(displayName) {
      def actionPerformed(e: ActionEvent): Unit = {
        createShadowWidget(coreWidget)
      }
    })

  // This is used both when loading a model and when the user is making
  // new widgets in the UI.  For most widget types, the same type string
  // is used in both places. - ST 3/17/04
  def makeWidget(widget: CoreWidget): Widget = {
    val widgetType = "Dummy " + widget.getClass.getSimpleName
    val fromRegistry = WidgetRegistry(widgetType)
    if (fromRegistry != null) {
      fromRegistry
    } else {
      widget match {
        case v: CoreView    => new DummyViewWidget(workspace.world)
        case c: CoreChooser => new DummyChooserWidget(new DefaultCompilerServices(workspace.compiler))
        case p: CorePlot    =>
          // note that plots on the HubNet client must have the name of a plot
          // on the server, thus, feed the dummy plot widget the names of
          // the current plots so the user can select one. We override
          // this method in InterfacePanel since regular plots are handled
          // differently ev 1/25/07
          val names = workspace.plotManager.getPlotNames
          DummyPlotWidget(names.headOption.getOrElse("plot 1"), workspace.plotManager)
        case i: CoreInputBox =>
          new DummyInputBoxWidget(
            new EditorArea(textEditorConfiguration),
            new EditorArea(dialogEditorConfiguration),
            this,
            new DefaultCompilerServices(workspace.compiler))
        case _ =>
          throw new IllegalStateException("unknown widget type: " + widget.getClass)
      }
    }
  }

  protected def textEditorConfiguration: EditorConfiguration =
    editorFactory.defaultConfiguration(1, 20)
      .withFont(NlogoFonts.monospacedFont)
      .withFocusTraversalEnabled(true)

  protected def dialogEditorConfiguration: EditorConfiguration =
    editorFactory.defaultConfiguration(5, 20)
      .withFont(NlogoFonts.monospacedFont)

  def mouseReleased(e: MouseEvent): Unit = {
    interactMode match {
      case InteractMode.INTERACT =>
        if (e.isPopupTrigger) {
          doPopup(e.getPoint)
        } else if (e.getButton == MouseEvent.BUTTON1) {
          if (NlogoMouse.hasCtrl(e)) {
            wrapperAtPoint(e.getPoint).foreach(wrapper => {
              setInteractMode(InteractMode.SELECT)
              selectWidget(wrapper, !wrapper.selected)
            })
          } else {
            unselectWidgets()
          }
        }

      case InteractMode.SELECT =>
        if (e.getButton == MouseEvent.BUTTON1 && selectionRect == null) {
          if (widgetsBeingDragged.nonEmpty) {
            wrapperAtPoint(e.getPoint).map(w => {
              e.translatePoint(-w.getX, -w.getY)
              w.mouseReleased(e)
            })
          } else {
            if (!NlogoMouse.hasCtrl(e))
              unselectWidgets()

            wrapperAtPoint(e.getPoint).foreach(wrapper => selectWidget(wrapper, !wrapper.selected))
          }
        } else {
          wrapperAtPoint(e.getPoint) match {
            case Some(w) =>
              e.translatePoint(-w.getX, -w.getY)
              w.mouseReleased(e)

            case _ =>
              if (e.isPopupTrigger)
                doPopup(e.getPoint)
          }
        }

        selectionRect = null
        selectionPane.setVisible(false)

      case InteractMode.ADD =>
        if (e.isPopupTrigger) {
          doPopup(e.getPoint)
        } else if (e.getButton == MouseEvent.BUTTON1) {
          placeShadowWidget()
        }

      case InteractMode.EDIT =>
        if (e.getButton == MouseEvent.BUTTON1) {
          new EditWidgetEvent(null).raise(this)
        } else {
          wrapperAtPoint(e.getPoint) match {
            case Some(w) =>
              e.translatePoint(-w.getX, -w.getY)
              w.mouseReleased(e)

            case _ =>
              if (e.isPopupTrigger)
                doPopup(e.getPoint)
          }
        }

      case InteractMode.DELETE =>
        if (e.getButton == MouseEvent.BUTTON1) {
          wrapperAtPoint(e.getPoint).foreach(wrapper => {
            if (wrapper.widget.deleteable)
              WidgetActions.removeWidget(this, wrapper)
          })
        } else {
          wrapperAtPoint(e.getPoint) match {
            case Some(w) =>
              e.translatePoint(-w.getX, -w.getY)
              w.mouseReleased(e)

            case _ =>
              if (e.isPopupTrigger)
                doPopup(e.getPoint)
          }
        }
    }
  }

  def keyPressed(e: KeyEvent): Unit = {
    if (interactMode == InteractMode.INTERACT) {
      if (System.getProperty("os.name").contains("Mac")) {
        if (e.getKeyCode == KeyEvent.VK_META)
          interceptPane.enableIntercept()
      } else if (e.getKeyCode == KeyEvent.VK_CONTROL) {
        interceptPane.enableIntercept()
      }
    }
  }

  def keyReleased(e: KeyEvent): Unit = {
    if (e.getKeyCode == KeyEvent.VK_ESCAPE) {
      setInteractMode(InteractMode.INTERACT)
    } else if (interactMode == InteractMode.INTERACT) {
      if (System.getProperty("os.name").contains("Mac")) {
        if (e.getKeyCode == KeyEvent.VK_META)
          interceptPane.disableIntercept()
      } else if (e.getKeyCode == KeyEvent.VK_CONTROL) {
        interceptPane.disableIntercept()
      }
    }
  }

  def keyTyped(e: KeyEvent): Unit = {}

  private[interfacetab] def setForegroundWrapper(): Unit =
    getComponents.collect {
      case w: WidgetWrapper if w.selected => w
    }.foreach(_.foreground())

  protected def selectWidget(wrapper: WidgetWrapper, selected: Boolean): Unit = {
    wrapper.selected(selected)

    setForegroundWrapper()
  }

  protected def selectWidgets(rect: Rectangle, persist: Boolean): Unit = {
    getComponents.collect {
      case w: WidgetWrapper => w
    }.foreach { wrapper =>
      import wrapper.selected
      val wrapperRect = wrapper.getUnselectedBounds
      if (! selected && rect.intersects(wrapperRect)) {
        wrapper.selected(true)
      } else if (!persist && selected && ! rect.intersects(wrapperRect)) {
        wrapper.selected(false)
      }
    }
    setForegroundWrapper()
  }

  protected def unselectWidgets(): Unit = {
    selectedWrappers.foreach(_.selected(false))
  }

  def addWidget(widget: Widget, x: Int, y: Int,
    select: Boolean, loadingWidget: Boolean): WidgetWrapper = {
    val size = widget.getSize()
    val wrapper = new WidgetWrapper(widget, this)
    wrapper.setVisible(false)
    // we need to add the wrapper before we can call wrapper.getPreferredSize(), because
    // that method looks at its parent and sees if it's an InterfacePanel
    // and zooms accordingly - ST 6/16/02
    add(wrapper, javax.swing.JLayeredPane.DEFAULT_LAYER)
    moveToFront(wrapper)

    if (select || ! loadingWidget) {
      wrapper.setSize(wrapper.getPreferredSize)
    } else {
      wrapper.setSize(size)
    }

    if (workspace.snapOn && ! loadingWidget) {
      val gridX = (x / GridSnap) * GridSnap
      val gridY = (y / GridSnap) * GridSnap
      wrapper.setLocation(gridX, gridY)
    } else {
      wrapper.setLocation(x, y)
    }

    wrapper.validate()
    wrapper.syncTheme()
    wrapper.setVisible(true)

    zoomer.zoomWidget(wrapper, true, loadingWidget, 1.0, zoomFactor)

    if (select) {
      newWidget = Some(wrapper)
      newWidget.get.originalBounds = newWidget.get.getBounds
    }
    LogManager.widgetAdded(loadingWidget, widget.classDisplayName, widget.displayName)
    wrapper
  }

  def reAddWidget(widgetWrapper: WidgetWrapper): WidgetWrapper = {
    widgetWrapper.setVisible(false)
    // we need to add the wrapper before we can call wrapper.getPreferredSize(), because
    // that method looks at its parent and sees if it's an InterfacePanel
    // and zooms accordingly - ST 6/16/02
    add(widgetWrapper, javax.swing.JLayeredPane.DEFAULT_LAYER)
    moveToFront(widgetWrapper)
    widgetWrapper.validate()
    widgetWrapper.syncTheme()
    widgetWrapper.setVisible(true)
    widgetWrapper.widget.reAdd()

    zoomer.zoomWidget(widgetWrapper, true, false, 1.0, zoomFactor)
    new CompileAllEvent().raise(this)
    LogManager.widgetAdded(false, widgetWrapper.widget.classDisplayName, widgetWrapper.widget.displayName)
    widgetWrapper
  }

  def createShadowWidget(widget: CoreWidget): Unit = {
    newShadowWidget(widget)

    stampWidget = false
  }

  def stampShadowWidget(widget: CoreWidget): Unit = {
    newShadowWidget(widget)

    stampWidget = true
  }

  def newShadowWidget(widget: CoreWidget): Unit = {
    val wrapper = new WidgetWrapper(makeWidget(widget), this)

    add(wrapper, JLayeredPane.DEFAULT_LAYER)

    moveToFront(wrapper)

    val mouse = MouseInfo.getPointerInfo.getLocation

    SwingUtilities.convertPointFromScreen(mouse, this)

    wrapper.setLocation(mouse.x.max(0), mouse.y.max(0))
    wrapper.setSize(wrapper.getPreferredSize)
    wrapper.validate()

    zoomer.zoomWidget(wrapper, true, false, 1.0, zoomFactor)

    setInteractMode(InteractMode.ADD)

    wrapper.enableFullShadow()

    wrapper.syncTheme()

    unselectWidgets()

    newWidget = Some(wrapper)
  }

  private def placeShadowWidget(): Unit = {
    interceptPane.disableIntercept()

    newWidget.foreach(widget => {
      val model = widget.widget.model

      widget.selected(true)
      widget.foreground()
      widget.isNew(true)
      widget.disableShadow()

      placedShadowWidget = true

      new EditWidgetEvent(null).raise(this)

      placedShadowWidget = false

      if (widget != null)
        widget.isNew(false)

      if (stampWidget)
        createShadowWidget(model)
    })
  }

  def removeShadowWidget(): Unit = {
    newWidget.foreach(widget => {
      removeWidget(widget)
      newWidget = None
      revalidate()
    })
  }

  def editWidgetFinished(target: Editable, canceled: Boolean): Unit = {
    target match {
      case comp: Component =>
        comp.getParent match {
          case ww: WidgetWrapper => ww.selected(false)
          case _ =>
        }
      case _ =>
    }
    if (canceled) {
      removeShadowWidget()
    } else {
      target match {
        case comp: Component =>
          comp.getParent match {
            case ww: WidgetWrapper =>
              WidgetActions.addWidget(this, ww)

              LogManager.widgetAdded(false, ww.widget.classDisplayName, ww.widget.displayName)
            case _ =>
          }
        case _ =>
      }
    }
    setForegroundWrapper()
  }

  def deleteSelectedWidgets(): Unit = {
    val hitList = selectedWrappers.filter {
      case w: WidgetWrapper => w.selected && w.widget.deleteable
      case _ => false
    }
    WidgetActions.removeWidgets(this, hitList)
  }

  def deleteWidget(target: WidgetWrapper): Unit =
    deleteWidgets(Seq(target))

  private[app] def deleteWidgets(hitList: Seq[WidgetWrapper]): Unit = {
    hitList.foreach(removeWidget)
    setForegroundWrapper()
    revalidate()
    repaint() // you wouldn't think this'd be necessary, but without it
    // the widget didn't visually disappear - ST 6/23/03
  }

  protected def removeWidget(wrapper: WidgetWrapper): Unit = {
    if (wrapper.widget eq view)
      view = null
    remove(wrapper)
    LogManager.widgetRemoved(false, wrapper.widget.classDisplayName, wrapper.widget.displayName)
  }

  private[interfacetab] def multiSelected: Boolean =
    selectedWrappers.length > 1

  // the following methods are helpers for the alignment and distribution tools (Isaac B 2/12/25)

  // returns true if the specified bounds do not overlap with any of the specified widgets (Isaac B 2/12/25)
  private def noCollision(bounds: Rectangle, existing: Seq[WidgetWrapper]): Boolean = {
    (existing ++ unselectedWrappers).forall { w =>
      bounds.x > w.widgetX + w.widgetWidth || bounds.x + bounds.width < w.widgetX ||
      bounds.y > w.widgetY + w.widgetHeight || bounds.y + bounds.height < w.widgetY
    }
  }

  // returns the widgets that can be moved without creating a collision (Isaac B 2/12/25)
  private def validWrappers(wrappers: Seq[WidgetWrapper], bounds: (WidgetWrapper) => Rectangle): Seq[WidgetWrapper] = {
    wrappers.foldLeft(Seq[WidgetWrapper]()) {
      case (existing, w) if noCollision(bounds(w), existing) => existing :+ w
      case (existing, _) => existing
    }
  }

  def alignLeft(): Unit = {
    val ordered = selectedWrappers.sortBy(_.getX)
    val target = ordered(0)

    WidgetActions.moveWidgets(validWrappers(ordered, (w) => {
      new Rectangle(target.widgetX, w.widgetY, w.widgetWidth, w.widgetHeight)
    }).map(w => (w, target.getX, w.getY)))
  }

  def alignCenterHorizontal(): Unit = {
    val target = selectedWrappers.head
    val x = (target.getX + target.getWidth / 2).max(selectedWrappers.maxBy(_.getWidth).getWidth / 2)

    WidgetActions.moveWidgets(selectedWrappers.map(w => (w, x - w.getWidth / 2, w.getY)))
  }

  def alignRight(): Unit = {
    val ordered = selectedWrappers.sortBy(w => w.getX + w.getWidth).reverse
    val target = ordered(0)

    WidgetActions.moveWidgets(validWrappers(ordered, (w) => {
      new Rectangle(target.widgetX + target.widgetWidth - w.widgetWidth, w.widgetY, w.widgetWidth, w.widgetHeight)
    }).map(w => (w, target.getX + target.getWidth - w.getWidth, w.getY)))
  }

  def alignTop(): Unit = {
    val ordered = selectedWrappers.sortBy(_.getY)
    val target = ordered(0)

    WidgetActions.moveWidgets(validWrappers(ordered, (w) => {
      new Rectangle(w.widgetX, target.widgetY, w.widgetWidth, w.widgetHeight)
    }).map(w => (w, w.getX, target.getY)))
  }

  def alignCenterVertical(): Unit = {
    val target = selectedWrappers.head
    val y = (target.getY + target.getHeight / 2).max(selectedWrappers.maxBy(_.getHeight).getHeight / 2)

    WidgetActions.moveWidgets(selectedWrappers.map(w => (w, w.getX, y - w.getHeight / 2)))
  }

  def alignBottom(): Unit = {
    val ordered = selectedWrappers.sortBy(w => w.getY + w.getHeight).reverse
    val target = ordered(0)

    WidgetActions.moveWidgets(validWrappers(ordered, (w) => {
      new Rectangle(w.widgetX, target.widgetY + target.widgetHeight - w.widgetHeight, w.widgetWidth, w.widgetHeight)
    }).map(w => (w, w.getX, target.getY + target.getHeight - w.getHeight)))
  }

  def distributeHorizontal(): Unit = {
    val ordered = selectedWrappers.sortBy(_.getX)
    val total = ordered.last.getX + ordered.last.getWidth - ordered.head.getX
    val space = (total - ordered.foldLeft(0)((c, w) => c + w.getWidth)) / (ordered.size - 1)

    var start = ordered(0).getX

    WidgetActions.moveWidgets(ordered.map { w =>
      val out = (w, start, w.getY)

      start += w.getWidth + space

      out
    })
  }

  def distributeVertical(): Unit = {
    val ordered = selectedWrappers.sortBy(_.getY)
    val total = ordered.last.getY + ordered.last.getHeight - ordered.head.getY
    val space = (total - ordered.foldLeft(0)((c, w) => c + w.getHeight)) / (ordered.size - 1)

    var start = ordered(0).getY

    WidgetActions.moveWidgets(ordered.map { w =>
      val out = (w, w.getX, start)

      start += w.getHeight + space

      out
    })
  }

  def stretchLeft(): Unit = {
    val target = selectedWrappers.minBy(_.getX)

    WidgetActions.reboundWidgets(selectedWrappers.map(w =>
      (w, new Rectangle(target.getX, w.getY, w.getX + w.getWidth - target.getX, w.getHeight))))
  }

  def stretchRight(): Unit = {
    val target = selectedWrappers.maxBy(w => w.getX + w.getWidth)

    WidgetActions.resizeWidgets(selectedWrappers.map(w => (w, target.getX + target.getWidth - w.getX, w.getHeight)))
  }

  def sliderEventOnReleaseOnly(sliderEventOnReleaseOnly: Boolean): Unit = {
    this.sliderEventOnReleaseOnly = sliderEventOnReleaseOnly
  }

  override def handle(e: ZoomedEvent): Unit = {
    super.handle(e)
    unselectWidgets()
    zoomer.zoomWidgets(zoomFactor)
    revalidate()
  }

  /// loading and saving

  def loadWidget(coreWidget: CoreWidget): Widget = {
    makeAndLoadWidget(coreWidget, coreWidget.x, coreWidget.y)
  }

  protected def makeAndLoadWidget(coreWidget: CoreWidget, x: Int, y: Int): Widget = {
    val newGuy = makeWidget(coreWidget)
    if (newGuy != null) {
      newGuy.load(coreWidget.asInstanceOf[newGuy.WidgetModel])
      enforceMinimumAndMaximumWidgetSizes(newGuy)
      addWidget(newGuy, x, y, false, true)
    }
    newGuy
  }

  def handle(e: WidgetEditedEvent): Unit = {
    new DirtyEvent(None).raise(this)
    zoomer.updateZoomInfo(e.widget)
  }

  def handle(e: WidgetRemovedEvent): Unit =
    e.widget match {
      case _: PlotWidget =>
        // since all plot widgets on the client are subordinate to
        // plot widgets on the server remove the plot widget
        // on the client when the plot in the server is removed
        // ev 1/18/07
        getComponents.foreach {
          case w: WidgetWrapper =>
            if (w.widget.isInstanceOf[DummyPlotWidget] && e.widget.displayName == w.widget.displayName)
              removeWidget(w)
          case _ =>
        }
        repaint()
      case _ =>
    }

  def handle(e: LoadBeginEvent): Unit = {
    setInteractMode(InteractMode.INTERACT)
    removeAllWidgets()
    zoomer.forgetAllZoomInfo()
    WidgetActions.undoManager.discardAllEdits()
  }

  override def loadWidgets(widgets: Seq[CoreWidget]): Unit = {
    try {
      if (widgets.nonEmpty) {
        setVisible(false)
        widgets.foreach(loadWidget)
      }
    } finally {
      setVisible(true)
      revalidate()
    }
  }

  override def getWidgetsForSaving: Seq[CoreWidget] =
    getComponents.reverse.collect {
      case w: WidgetWrapper => w.widget.model
    }.distinct.toSeq

  override def allWidgets: Seq[CoreWidget] =
    getWidgetsForSaving

  override def removeAllWidgets(): Unit = {
    val comps = getComponents
    setVisible(false)
    comps.foreach {
      case w: WidgetWrapper => removeWidget(w)
      case _ =>
    }
  }

  override def hasView: Boolean = view != null

  private[app] def contains(w: Editable): Boolean = {
    val isContained = getComponents.exists {
      case ww: WidgetWrapper => ww.widget.getEditable == w
      case _                 => false
    }
    isContained
  }

  protected def enforceMinimumAndMaximumWidgetSizes(component: Component): Unit = {
    val size = component.getSize
    var changed = false

    // enforce minimum
    val minimumSize = component.getMinimumSize

    if (size.width < minimumSize.width) {
      size.width = minimumSize.width
      changed = true
    }
    if (size.height < minimumSize.height) {
      size.height = minimumSize.height
      changed = true
    }

    // enforce maximum
    val maximumSize = component.getMaximumSize
    if (maximumSize != null) {
      if (size.width > maximumSize.width && maximumSize.width > 0) {
        size.width = maximumSize.width
        changed = true
      }
      if (size.height > maximumSize.height && maximumSize.height > 0) {
        size.height = maximumSize.height
        changed = true
      }
    }

    if (changed)
      component.setSize(size)
  }

  /// dispatch WidgetContainer methods

  def getUnzoomedBounds(component: Component): Rectangle =
    zoomer.getUnzoomedBounds(component)

  def resetZoomInfo(widget: Widget): Unit = {
    zoomer.updateZoomInfo(widget)
  }

  def resetSizeInfo(widget: Widget): Unit = {
    getWrapper(widget).widgetResized()
  }

  def isZoomed: Boolean =
    zoomer.zoomFactor != 1.0

  def canAddWidget(widget: String): Boolean = {
    if (widget.equals(I18N.gui.get("tabs.run.widgets.view"))) {
      !hasView
    } else if (widget.equals(I18N.gui.get("tabs.run.widgets.plot"))) {
      // you can't add a plot to the client interface unless
      // there are plots in the server interface so enable the
      // plot button accordingly ev 1/25/07
      workspace.plotManager.getPlotNames.length > 0
    } else {
      true
    }
  }

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.interfaceBackground)

    setCursor(interactMode.cursor)

    getWrappers.foreach(_.syncTheme())

    // for code completion popup
    editorFactory.syncTheme()
  }
}
