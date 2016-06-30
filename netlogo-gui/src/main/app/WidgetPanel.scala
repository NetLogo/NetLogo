// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.{ Editable, Version }
import org.nlogo.window.{ AbstractWidgetPanel, ButtonWidget, CodeEditor,
  DummyButtonWidget, DummyChooserWidget, DummyInputBoxWidget, DummyMonitorWidget,
  DummyPlotWidget, DummySliderWidget, DummyViewWidget, EditorColorizer,
  GUIWorkspace, OutputWidget, PlotWidget, Widget, WidgetContainer, WidgetRegistry }
import org.nlogo.window.Events.{ DirtyEvent,
  EditWidgetEvent, WidgetEditedEvent, WidgetRemovedEvent, LoadBeginEvent, ZoomedEvent }
import org.nlogo.core.{ I18N, Widget => CoreWidget,
  Button => CoreButton, Chooser => CoreChooser, InputBox => CoreInputBox,
  Monitor => CoreMonitor, Plot => CorePlot, Slider => CoreSlider,
  Switch => CoreSwitch, TextBox => CoreTextBox, View => CoreView }
import org.nlogo.core.model.WidgetReader
import org.nlogo.fileformat
import org.nlogo.awt.{ Mouse => NlogoMouse, Fonts => NlogoFonts }
import org.nlogo.nvm.DefaultCompilerServices

import org.nlogo.log.Logger
import java.awt.event.{ ActionListener, ActionEvent,
  MouseListener, MouseMotionListener, FocusListener, FocusEvent }
import javax.swing.{ JComponent, JMenuItem, JPopupMenu }
import javax.swing.JLayeredPane.DRAG_LAYER
import java.awt.event.MouseEvent
import java.awt.{ Color => AwtColor, Component, Cursor, Dimension, Rectangle, Point, Graphics }

import scala.collection.JavaConverters._

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
    with FocusListener
    with WidgetEditedEvent.Handler
    with WidgetRemovedEvent.Handler
    with LoadBeginEvent.Handler {

  import WidgetPanel.GridSnap

  private[app] var _hasFocus: Boolean = false
  protected var selectionRect: Rectangle = null // convert to Option?
  protected var widgetCreator: WidgetCreator = null
  protected var widgetsBeingDragged: Seq[WidgetWrapper] = Seq()
  private var view: Widget = null // convert to Option?

  // if sliderEventOnReleaseOnly is true, a SliderWidget will only raise an InterfaceGlobalEvent
  // when the mouse is released from the SliderDragControl
  // --mag 9/25/02, ST 4/9/03
  var sliderEventOnReleaseOnly: Boolean = false

  protected var startDragPoint: Point = null // convert to Option?
  protected var newWidget: WidgetWrapper = null // convert to Option?
  protected var glassPane: JComponent =
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

  setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
  setOpaque(true)
  setBackground(AwtColor.WHITE)
  addMouseListener(this)
  addMouseMotionListener(this)
  addFocusListener(this)
  setAutoscrolls(true)
  glassPane.setOpaque(false)
  glassPane.setVisible(false)
  add(glassPane, DRAG_LAYER)

  // our children may overlap
  override def isOptimizedDrawingEnabled: Boolean = false

  override def requestFocus(): Unit = {
    requestFocusInWindow()
  }

  def focusGained(e: FocusEvent): Unit = {
    _hasFocus = true
  }

  def focusLost(e: FocusEvent): Unit = {
    _hasFocus = false
  }

  override def getMinimumSize: Dimension =
    new java.awt.Dimension(0, 0)

  override def getPreferredSize: Dimension = {
    var maxX = 0
    var maxY = 0
    for { component <- getComponents if component ne glassPane } {
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

  def setWidgetCreator(widgetCreator: WidgetCreator): Unit = {
    this.widgetCreator = widgetCreator
  }

  ///

  def getWrapper(widget: Widget): WidgetWrapper =
    widget.getParent.asInstanceOf[WidgetWrapper]

  protected def selectedWrappers: Seq[WidgetWrapper] =
    getComponents.collect {
      case w: WidgetWrapper if w.selected => w
    }

  protected def aboutToDragSelectedWidgets(startPressX: Int, startPressY: Int): Unit = {
    widgetsBeingDragged = selectedWrappers
    widgetsBeingDragged.foreach { w =>
      w.aboutToDrag(startPressX, startPressY)
    }
  }

  protected def dragSelectedWidgets(x: Int, y: Int): Unit = {
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

    if (newWb.x + newWb.width < WidgetWrapper.BORDER_E * 2)
      x += WidgetWrapper.BORDER_E * 2 - (newWb.x + newWb.width)
    if (newWb.y < WidgetWrapper.BORDER_N)
      y += WidgetWrapper.BORDER_N - newWb.y
    if (newWb.x + 2 * WidgetWrapper.BORDER_W > b.width)
      x -= (newWb.x + 2 * WidgetWrapper.BORDER_W) - b.width
    if (newWb.y + WidgetWrapper.BORDER_N > b.height)
      y -= (newWb.y + WidgetWrapper.BORDER_N) - b.height

    new Point(x, y)
  }

  protected def dropSelectedWidgets(): Unit = {
    widgetsBeingDragged.foreach(_.doDrop())
    widgetsBeingDragged = Seq()
    setForegroundWrapper()
  }

  def mouseMoved(e: MouseEvent): Unit = { }

  def mouseDragged(e: MouseEvent): Unit =
    if (NlogoMouse.hasButton1(e)) {
      val p = e.getPoint
      val rect = getBounds()

      p.x += rect.x
      p.y += rect.y

      if (newWidget != null) {
        if (workspace.snapOn) {
          startDragPoint.x = (startDragPoint.x / GridSnap) * GridSnap
          startDragPoint.y = (startDragPoint.y / GridSnap) * GridSnap
        }
        val p2 = restrictDrag(new Point(e.getX - startDragPoint.x, e.getY - startDragPoint.y), newWidget)
        newWidget.setLocation(startDragPoint.x + p2.x, startDragPoint.y + p2.y)
      } else if (null != startDragPoint) {
        if (!glassPane.isVisible()) {
          glassPane.setBounds(0, 0, getWidth(), getHeight())
          glassPane.setVisible(true)
        }
        scrollRectToVisible(new Rectangle(e.getX - 20, e.getY - 20, 40, 40))
        val oldSelectionRect = selectionRect
        val x = StrictMath.min(getWidth, StrictMath.max(e.getX, 0))
        val y = StrictMath.min(getHeight, StrictMath.max(e.getY, 0))
        selectionRect =
          new java.awt.Rectangle(
            StrictMath.min(startDragPoint.x, x),
            StrictMath.min(startDragPoint.y, y),
            StrictMath.abs(x - startDragPoint.x),
            StrictMath.abs(y - startDragPoint.y)
          )
          selectWidgets(selectionRect)
          if (oldSelectionRect != null)
            glassPane.repaint(oldSelectionRect)
          glassPane.repaint(selectionRect)
      }
    }

  def mouseEntered(e: MouseEvent): Unit = { }
  def mouseExited(e: MouseEvent): Unit = { }
  def mouseClicked(e: MouseEvent): Unit = { }

  def mousePressed(e: MouseEvent): Unit =
    if (e.isPopupTrigger)
      doPopup(e)
    else if (NlogoMouse.hasButton1(e)) {
      // this is so the user can use action keys to control buttons
      // - ST 8/6/04,8/31/04
      requestFocus()

      val p = e.getPoint
      val rect = getBounds()

      p.x += rect.x
      p.y += rect.y

      if (rect.contains(p)) {
        unselectWidgets()
        startDragPoint = e.getPoint

        if (widgetCreator != null) {
          val widget = widgetCreator.getWidget
          if (widget != null) {
            addWidget(widget, e.getX, e.getY, true, false)
            revalidate()
          }
        }
      }
    }

  // this is bordering on comical its so confusing.
  // this method runs for the hubnet client editor.
  // im not yet sure if it runs anywhere else.
  // that seems like bugs waiting to happen. JC - 12/20/10
  protected def doPopup(e: MouseEvent): Unit = {
    val menu = new JPopupMenu()
    def menuItem(keyName: String, widget: CoreWidget): WidgetCreationMenuItem = {
      new WidgetCreationMenuItem(I18N.gui.get(s"tabs.run.widgets.$keyName"), widget, e.getX, e.getY)
    }
    val plot = menuItem("plot", CorePlot(None))
    val menuItems = Seq(
      menuItem("button", CoreButton(None, 0, 0, 0, 0)),
      menuItem("slider", CoreSlider(None)),
      menuItem("switch", CoreSwitch(None)),
      menuItem("chooser", CoreChooser(None)),
      menuItem("input", CoreInputBox(None)),
      menuItem("monitor", CoreMonitor(None, 0, 0, 0, 0, None, 10)),
      plot,
      menuItem("note", CoreTextBox(None, fontSize = 11, color = 0)))
    menuItems.foreach(menu.add)

    // if there are no plots in this model, then you can't have a plot in a hubnet client.
    if (workspace.plotManager.plots.size == 0)
      plot.setEnabled(false)

    menu.show(this, e.getX, e.getY)
  }

  protected class WidgetCreationMenuItem(displayName: String, coreWidget: CoreWidget, x: Int, y: Int)
    extends JMenuItem(displayName) with ActionListener {
    addActionListener(this)

    override def actionPerformed(e: ActionEvent): Unit = {
      createWidget(coreWidget, x, y)
    }
  }

  def createWidget(coreWidget: CoreWidget, x: Int, y: Int): Unit = {
    val widget = makeWidget(coreWidget)
    val wrapper = addWidget(widget, x, y, true, false)
    revalidate()
    wrapper.selected(true)
    wrapper.foreground()
    wrapper.isNew(true)
    new EditWidgetEvent(null).raise(WidgetPanel.this)
    newWidget.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
    wrapper.isNew(false)
    newWidget = null // TODO: new widget set somewhere else and nulled here, gross
  }

  // This is used both when loading a model and when the user is making
  // new widgets in the UI.  For most widget types, the same type string
  // is used in both places. - ST 3/17/04
  def makeWidget(widget: CoreWidget): Widget = {
    val widgetType = "Dummy " + widget.getClass.getSimpleName
    val fromRegistry = WidgetRegistry(widgetType)
    if (fromRegistry != null)
      fromRegistry
    else
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
          val font = NlogoFonts.monospacedFont
          new DummyInputBoxWidget(
            new CodeEditor(1, 20, font, true,  null, new EditorColorizer(workspace), I18N.gui.fn),
            new CodeEditor(5, 20, font, false, null, new EditorColorizer(workspace), I18N.gui.fn),
            this,
            new DefaultCompilerServices(workspace.compiler))
        case _ =>
          throw new IllegalStateException("unknown widget type: " + widget.getClass)
      }
  }

  def mouseReleased(e: MouseEvent): Unit =
    if (e.isPopupTrigger)
      doPopup(e)
    else {
      if (NlogoMouse.hasButton1(e)) {
        val p = e.getPoint
        val rect = getBounds()

        p.x += rect.x
        p.y += rect.y

        selectionRect = null
        glassPane.setVisible(false)

        if (newWidget != null) {
          newWidget.selected(true)
          newWidget.foreground()
          newWidget.isNew(true)
          new EditWidgetEvent(null).raise(this)
          newWidget.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
          newWidget.isNew(false)
          newWidget = null
        }
      }
    }

  protected def setForegroundWrapper(): Unit =
    getComponents.collect {
      case w: WidgetWrapper if w.selected => w
    }.foreach(_.foreground())

  protected def selectWidgets(rect: Rectangle): Unit = {
    getComponents.collect {
      case w: WidgetWrapper => w
    }.foreach { wrapper =>
      import wrapper.selected
      val wrapperRect = wrapper.getUnselectedBounds
      if (! selected && rect.intersects(wrapperRect))
        wrapper.selected(true)
      else if (selected && ! rect.intersects(wrapperRect))
        wrapper.selected(false)
    }
    setForegroundWrapper()
  }

  protected def unselectWidgets(): Unit = {
    selectedWrappers.foreach(_.selected(false))
  }

  protected def addWidget(widget: Widget, x: Int, y: Int,
    select: Boolean, loadingWidget: Boolean): WidgetWrapper = {
    val size = widget.getSize()
    val wrapper = new WidgetWrapper(widget, this)
    wrapper.setVisible(false)
    // we need to add the wrapper before we can call wrapper.getPreferredSize(), because
    // that method looks at its parent and sees if it's an InterfacePanel
    // and zooms accordingly - ST 6/16/02
    add(wrapper, javax.swing.JLayeredPane.DEFAULT_LAYER)
    moveToFront(wrapper)

    if (select || ! loadingWidget)
      wrapper.setSize(wrapper.getPreferredSize())
    else
      wrapper.setSize(size)

    if (workspace.snapOn && ! loadingWidget) {
      val gridX = (x / GridSnap) * GridSnap
      val gridY = (y / GridSnap) * GridSnap
      wrapper.setLocation(gridX, gridY)
    } else {
      wrapper.setLocation(x, y)
    }

    wrapper.validate()
    wrapper.setVisible(true)

    zoomer.zoomWidget(wrapper, true, loadingWidget, 1.0, zoomFactor)

    if (select) {
      newWidget = wrapper
      newWidget.originalBounds = newWidget.getBounds
      newWidget.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
    }
    Logger.logAddWidget(widget.classDisplayName, widget.displayName)
    wrapper
  }

  def editWidgetFinished(target: Editable, canceled: Boolean): Unit = {
    target match {
      case comp: Component if comp.getParent.isInstanceOf[WidgetWrapper] =>
        comp.getParent.asInstanceOf[WidgetWrapper].selected(false)
      case _ =>
    }
    if (canceled && newWidget != null) {
      removeWidget(newWidget)
      revalidate()
    }
    setForegroundWrapper()
    // this doesn't do anything on the Mac, presumably because the focus doesn't never
    // actually returns to us after the edit dialog closed because isFocusable() is
    // already false, but just in case it's needed on some VM... - ST 8/6/04
    loseFocusIfAppropriate()
  }

  def deleteSelectedWidgets(): Unit = {
    val hitList = selectedWrappers.filter {
      case w: WidgetWrapper => w.selected && w.widget.deleteable
      case _ => false
    }
    deleteWidgets(hitList)
  }

  protected def deleteWidget(target: WidgetWrapper): Unit =
    deleteWidgets(Seq(target))

  private[app] def deleteWidgets(hitList: Seq[WidgetWrapper]): Unit = {
    hitList.foreach(removeWidget)
    setForegroundWrapper()
    revalidate()
    repaint() // you wouldn't think this'd be necessary, but without it
    // the widget didn't visually disappear - ST 6/23/03
    loseFocusIfAppropriate()
  }

  protected def removeWidget(wrapper: WidgetWrapper): Unit = {
    if (wrapper.widget eq view)
      view = null
    remove(wrapper)
    Logger.logWidgetRemoved(wrapper.widget.classDisplayName, wrapper.widget.displayName)
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
    makeAndLoadWidget(coreWidget, coreWidget.left, coreWidget.top)
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
    new DirtyEvent().raise(this)
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
    unselectWidgets()
    removeAllWidgets()
    zoomer.forgetAllZoomInfo()
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

  /// buttons

  protected def loseFocusIfAppropriate(): Unit = {
    if (_hasFocus && !isFocusable)
      transferFocus()
  }

  override def isFocusable: Boolean =
    getComponents.exists {
      case w: WidgetWrapper if w.widget.isInstanceOf[ButtonWidget] =>
        val key = w.widget.asInstanceOf[ButtonWidget].actionKey
        key != '\u0000' && key != ' '
      case _ => false
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
    if (widget.equals(I18N.gui.get("tabs.run.widgets.view")))
      ! hasView
    else if (widget.equals(I18N.gui.get("tabs.run.widgets.plot")))
      // you can't add a plot to the client interface unless
      // there are plots in the server interface so enable the
      // plot button accordingly ev 1/25/07
      workspace.plotManager.getPlotNames.length > 0
    else
      true
  }
}
