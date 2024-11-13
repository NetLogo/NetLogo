// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Color, Component, Cursor, Dimension, Graphics, Point, Rectangle }
import java.awt.event.{ ActionEvent, InputEvent, MouseAdapter, MouseEvent, MouseListener,  MouseMotionAdapter,
                        MouseMotionListener }
import javax.swing.{ AbstractAction, JComponent, JLayeredPane, JPanel, JPopupMenu }

import org.nlogo.api.Editable
import org.nlogo.app.common.Events.WidgetSelectedEvent
import org.nlogo.awt.{ Coordinates, Mouse }
import org.nlogo.core.I18N
import org.nlogo.swing.{ MenuItem, RoundedBorderPanel, WrappingPopupMenu }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ MouseMode, Widget, WidgetWrapperInterface }
import org.nlogo.window.Events.{ DirtyEvent, EditWidgetEvent, ExportWidgetEvent, WidgetForegroundedEvent }

object WidgetWrapper {
  private val NON_FOREGROUND_BACKGROUND = new Color(205, 205, 205)

  val BORDER_N = 10
  val BORDER_S = 9
  val BORDER_E = 9
  val BORDER_W = 9

  val HANDLE_WIDTH = 9

  private val MIN_WIDGET_WIDTH = 12
  private val MIN_WIDGET_HEIGHT = 12
}

// public for widget extension - ST 6/12/08
class WidgetWrapper(widget: Widget, val interfacePanel: WidgetPanel)
  extends JLayeredPane
  with WidgetWrapperInterface
  with MouseListener
  with MouseMotionListener
  with WidgetForegroundedEvent.Handler
  with ThemeSync {
  
  private var _verticallyResizable = false
  private var horizontallyResizable = false
  private var _isNew = false
  private var _selected = false
  private var _isForeground = false
  private var mouseMode = MouseMode.IDLE
  private var startPressX = 0
  private var startPressY = 0
  private var constrainToHorizontal = false
  private var constrainToVertical = false

  private val topBar = new WidgetWrapperEdge(WidgetWrapperEdge.TOP, WidgetWrapper.BORDER_E, WidgetWrapper.BORDER_W,
                                             _verticallyResizable)
  private val leftBar = new WidgetWrapperEdge(WidgetWrapperEdge.SIDE, 0, 0, horizontallyResizable)
  private val rightBar = new WidgetWrapperEdge(WidgetWrapperEdge.SIDE, 0, 0, horizontallyResizable)
  private val bottomBar = new WidgetWrapperEdge(WidgetWrapperEdge.BOTTOM, WidgetWrapper.BORDER_E,
                                                WidgetWrapper.BORDER_W, _verticallyResizable)

  private val glass = new JComponent {}

  glass.setOpaque(false)
  glass.addMouseListener(this)
  glass.addMouseMotionListener(this)

  def setShadow(shadow: Boolean) {
    shadowPane.setVisible(shadow)

    revalidate()
  }

  private val shadowPane = new JPanel with RoundedBorderPanel with ThemeSync {
    setBorderColor(InterfaceColors.TRANSPARENT)
    setVisible(false)

    override def paintComponent(g: Graphics) {
      setDiameter(12 * widget.getZoomFactor)

      super.paintComponent(g)
    }

    def syncTheme() {
      if (widget.isNote)
        setBackgroundColor(InterfaceColors.WIDGET_PREVIEW_COVER_NOTE)
      else
        setBackgroundColor(InterfaceColors.WIDGET_PREVIEW_COVER)
    }
  }

  var originalBounds: Rectangle = null

  // this is for notes, when the bg is transparent we don't want to see the
  // white widget wrapper.  I don't know why setting the background to a
  // transparent color doesn't work.  but it doesn't ev 6/8/07
  setOpaque(false)

  setBackground(widget.getBackground)
  setCursor(Cursor.getDefaultCursor)
  setLayout(null)

  add(glass, JLayeredPane.DRAG_LAYER)
  add(widget)
  add(shadowPane, JLayeredPane.PALETTE_LAYER)
  add(topBar)
  add(leftBar)
  add(rightBar)
  add(bottomBar)

  doLayout()

  widgetChanged() // update cornerHandles and otherwise make sure we are in good state -- CLB

  // don't let mouse events get through to the InterfacePanel
  // (is there a more elegant way to do this?) - ST 8/9/03
  addMouseListener(new MouseAdapter {})
  addMouseMotionListener(new MouseMotionAdapter {})
  
  widget.addPopupListeners(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      if (e.isPopupTrigger)
        doPopup(e)
    }

    override def mouseReleased(e: MouseEvent) {
      if (e.isPopupTrigger)
        doPopup(e)
    }
  })

  def widget(): Widget =
    widget

  def verticallyResizable: Boolean =
    _verticallyResizable

  def isNew: Boolean =
    _isNew

  def isNew(isNew: Boolean) {
    _isNew = isNew
  }

  def computeVerticallyResizable: Boolean =
    widget.getMaximumSize == null || widget.getMaximumSize.height != widget.getMinimumSize.height

  def computeHorizontallyResizable: Boolean =
    widget.getMaximumSize == null || widget.getMaximumSize.width != widget.getMinimumSize.width

  def widgetChanged() {
    _verticallyResizable = computeVerticallyResizable
    horizontallyResizable = computeHorizontallyResizable

    if (_verticallyResizable) {
      topBar.handles(true)
      bottomBar.handles(true)
    }
    
    else {
      topBar.handles(false)
      bottomBar.handles(false)
    }

    if (horizontallyResizable) {
      topBar.cornerHandles(true)
      bottomBar.cornerHandles(true)
      leftBar.handles(true)
      rightBar.handles(true)
    }
    
    else {
      topBar.cornerHandles(false)
      bottomBar.cornerHandles(false)
      leftBar.handles(false)
      rightBar.handles(false)
    }
  }

  override def isValidateRoot: Boolean =
    true

  def selected: Boolean =
    _selected

  def selected(selected: Boolean) {
    this.selected(selected, false)
  }

  def selected(selected: Boolean, temporary: Boolean) {
    if (_selected != selected) {
      _selected = selected

      if (selected) {
        setBounds(getX - WidgetWrapper.BORDER_E, getY - WidgetWrapper.BORDER_N,
                  getWidth + WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W,
                  getHeight + WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S)
      }

      else {
        isForeground(false)
        setBounds(getX + WidgetWrapper.BORDER_E, getY + WidgetWrapper.BORDER_N,
                  getWidth - WidgetWrapper.BORDER_E - WidgetWrapper.BORDER_W,
                  getHeight - WidgetWrapper.BORDER_N - WidgetWrapper.BORDER_S)
      }

      if (!temporary)
        new WidgetSelectedEvent(widget, selected).raise(this)
    }
  }

  private def revalidateInterfacePanel() {
    if (interfacePanel != null)
      interfacePanel.revalidate()
  }

  override def setBounds(r: Rectangle) {
    setBounds(r.x, r.y, r.width, r.height)
  }

  override def setBounds(x: Int, y: Int, width: Int, height: Int) {
    val sizeChanged = getWidth != width || getHeight != height

    super.setBounds(x, y, width, height)

    if (sizeChanged) {
      doLayout()
      revalidateInterfacePanel()
    }
  }

  def isForeground: Boolean =
    _isForeground

  def isForeground(isForeground: Boolean) {
    _isForeground = isForeground

    if (isForeground) {
      topBar.setBackground(Color.GRAY)
      leftBar.setBackground(Color.GRAY)
      rightBar.setBackground(Color.GRAY)
      bottomBar.setBackground(Color.GRAY)

      topBar.setForeground(Color.BLACK)
      leftBar.setForeground(Color.BLACK)
      rightBar.setForeground(Color.BLACK)
      bottomBar.setForeground(Color.BLACK)
    }
    
    else {
      topBar.setBackground(WidgetWrapper.NON_FOREGROUND_BACKGROUND)
      leftBar.setBackground(WidgetWrapper.NON_FOREGROUND_BACKGROUND)
      rightBar.setBackground(WidgetWrapper.NON_FOREGROUND_BACKGROUND)
      bottomBar.setBackground(WidgetWrapper.NON_FOREGROUND_BACKGROUND)

      topBar.setForeground(Color.GRAY)
      leftBar.setForeground(Color.GRAY)
      rightBar.setForeground(Color.GRAY)
      bottomBar.setForeground(Color.GRAY)
    }

    topBar.repaint()
    leftBar.repaint()
    rightBar.repaint()
    bottomBar.repaint()
  }

  def foreground() {
    if (!isForeground) {
      isForeground(true)

      new WidgetForegroundedEvent(widget).raise(this)
    }
  }

  private def addWrapperBorder(dim: Dimension): Dimension = {
    // some widgets have no max size.
    // Adding the border dimensions to that results in another null -- CLB
    if (dim == null)
      return null

    val newDim =
      getParent match {
        case wp: WidgetPanel if !widget.isNote => wp.zoomer.zoomSize(dim)
        case _ => dim
      }

    if (selected)
      new Dimension(newDim.width + WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W,
                    newDim.height + WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S)
    else
      newDim
  }

  override def getPreferredSize: Dimension = {
    addWrapperBorder(
      if (widget.isNote)
        widget.getPreferredSize
      else
        widget.getUnzoomedPreferredSize
    )
  }

  override def getMaximumSize: Dimension =
    addWrapperBorder(widget.getMaximumSize)

  override def doLayout() {
    if (selected) {
      topBar.setVisible(true)
      leftBar.setVisible(true)
      rightBar.setVisible(true)
      bottomBar.setVisible(true)

      topBar.setBounds(0, 0, getWidth, WidgetWrapper.BORDER_N)
      leftBar.setBounds(0, WidgetWrapper.BORDER_N, WidgetWrapper.BORDER_E,
                        getHeight - WidgetWrapper.BORDER_N - WidgetWrapper.BORDER_S)
      rightBar.setBounds(getWidth - WidgetWrapper.BORDER_W, WidgetWrapper.BORDER_N, WidgetWrapper.BORDER_W,
                         getHeight - WidgetWrapper.BORDER_N - WidgetWrapper.BORDER_S)
      bottomBar.setBounds(0, getHeight - WidgetWrapper.BORDER_S, getWidth, WidgetWrapper.BORDER_S)

      widget.setBounds(WidgetWrapper.BORDER_E, WidgetWrapper.BORDER_N,
                       getWidth - WidgetWrapper.BORDER_E - WidgetWrapper.BORDER_W,
                       getHeight - WidgetWrapper.BORDER_N - WidgetWrapper.BORDER_S)
    }
    
    else {
      topBar.setVisible(false)
      leftBar.setVisible(false)
      rightBar.setVisible(false)
      bottomBar.setVisible(false)

      topBar.setBounds(0, 0, 0, 0)
      leftBar.setBounds(0, 0, 0, 0)
      rightBar.setBounds(0, 0, 0, 0)
      bottomBar.setBounds(0, 0, 0, 0)

      widget.setBounds(0, 0, getWidth, getHeight)
    }

    glass.setBounds(0, 0, getWidth, getHeight)
    glass.setVisible(selected)

    shadowPane.setBounds(0, 0, getWidth, getHeight)
  }

  /*
  private def mouseMode_=(mouseMode: MouseMode) {
    it would be nice if it were this easy, but it's not, because setCursor only sets
    the cursor for when the mouse is actually over the component... at least in Java
    1.1, there's no way to globally set the cursor, which is what we want - ST 1/20/02
    switch( mouseMode )
    {
    case MOUSE_IDLE :
    case MOUSE_DRAG :
    setCursor( java.awt.Cursor.getDefaultCursor() ) ;
    break ;
    case MouseMode.NE :
    setCursor( java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.NE_RESIZE_CURSOR ) ) ;
    break ;
    case MouseMode.NW :
    setCursor( java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.NW_RESIZE_CURSOR ) ) ;
    break ;
    case MouseMode.SE :
    setCursor( java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.SE_RESIZE_CURSOR ) ) ;
    break ;
    case MouseMode.SW :
    setCursor( java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.SW_RESIZE_CURSOR ) ) ;
    break ;
    case MouseMode.S :
    setCursor( java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.S_RESIZE_CURSOR ) ) ;
    break ;
    case MouseMode.W :
    setCursor( java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.W_RESIZE_CURSOR ) ) ;
    break ;
    case MouseMode.E :
    setCursor( java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.E_RESIZE_CURSOR ) ) ;
    break ;
    case MouseMode.N :
    setCursor( java.awt.Cursor.getPredefinedCursor( java.awt.Cursor.N_RESIZE_CURSOR ) ) ;
    break ;
    }
  }
  */

  def doResize(x: Int, y: Int) {
    /* x and y represent the distance from the original click and the dragged cursor position,
        so the widget can resize based on the position of the cursor. Interestingly, the
        x and y can be negative since the difference is calculated from the coordinates.
        Also, the bounds.x and bounds.width refer to the original bounds before resizing began
        and will be updated through the new rectangle initialized below. CBR 01/09/19.
      */
    val bounds = new Rectangle(originalBounds)

    mouseMode match {
      case MouseMode.NW =>
        val newY = y.max(WidgetWrapper.BORDER_N - bounds.y)
        val newX = x.max(-bounds.x)

        bounds.x += newX
        bounds.width -= newX
        bounds.y += newY
        bounds.height -= newY

      case MouseMode.NE =>
        val newY = y.max(WidgetWrapper.BORDER_N - bounds.y)
        val newX = x.max(-bounds.x - bounds.width)

        bounds.width += newX
        bounds.y += newY
        bounds.height -= newY
        
      case MouseMode.SW =>
        val newX = x.max(-bounds.x)

        bounds.x += newX
        bounds.width -= newX
        bounds.height += y
        
      case MouseMode.W =>
        val newX = x.max(-bounds.x)

        bounds.x += newX
        bounds.width -= newX

      case MouseMode.SE =>
        bounds.width += x.max(-bounds.x - bounds.width)
        bounds.height += y

      case MouseMode.E =>
        bounds.width += x.max(-bounds.x - bounds.width)

      case MouseMode.S =>
        bounds.height += y

      case MouseMode.N =>
        val newY = y.max(WidgetWrapper.BORDER_N - bounds.y)

        bounds.y += newY
        bounds.height -= newY

      case _ => throw new IllegalStateException
    }

    if (interfacePanel.workspace.snapOn && !interfacePanel.isZoomed)
      enforceGridSnapSize(bounds)

    enforceMinimumSize(bounds)
    enforceMaximumSize(bounds)

    setBounds(widget.constrainDrag(bounds, originalBounds, mouseMode))
  }

  def gridSnap: Int = {
    if (interfacePanel.workspace.snapOn)
      WidgetPanel.GridSnap
    else
      1
  }

  def mouseMoved(e: MouseEvent) {}
  def mouseClicked(e: MouseEvent) {}
  def mouseEntered(e: MouseEvent) {}
  def mouseExited(e: MouseEvent) {}

  def mousePressed(e: MouseEvent) {
    if (e.isPopupTrigger && mouseMode != MouseMode.DRAG) {
      doPopup(e)

      return
    }

    if (!Mouse.hasButton1(e))
      return

    foreground()

    if (e.getClickCount == 2) {
      new EditWidgetEvent(null).raise(this)

      return
    }
    
    val x = e.getX
    val y = e.getY

    val p = Coordinates.convertPointToScreen(e.getPoint, this)

    startPressX = p.x
    startPressY = p.y

    mouseMode = MouseMode.DRAG

    if (x < WidgetWrapper.BORDER_W) {
      if (_verticallyResizable && y < WidgetWrapper.BORDER_N)
        mouseMode = MouseMode.NW
      else if (_verticallyResizable && y > getHeight - WidgetWrapper.BORDER_S)
        mouseMode = MouseMode.SW
      else if (y <= WidgetWrapper.BORDER_N + (getHeight - WidgetWrapper.BORDER_S - WidgetWrapper.BORDER_N -
                                              WidgetWrapper.HANDLE_WIDTH) / 2 + WidgetWrapper.HANDLE_WIDTH &&
               y >= WidgetWrapper.BORDER_N + (getHeight - WidgetWrapper.BORDER_S - WidgetWrapper.BORDER_N -
                                              WidgetWrapper.HANDLE_WIDTH) / 2)
        mouseMode = MouseMode.W
    }
    
    else if (x > getWidth - WidgetWrapper.BORDER_E) {
      if (_verticallyResizable && y < WidgetWrapper.BORDER_N)
        mouseMode = MouseMode.NE
      else if (_verticallyResizable && y > getHeight - WidgetWrapper.BORDER_S)
        mouseMode = MouseMode.SE
      else if (y <= WidgetWrapper.BORDER_N + (getHeight - WidgetWrapper.BORDER_S - WidgetWrapper.BORDER_N -
                                              WidgetWrapper.HANDLE_WIDTH) / 2 + WidgetWrapper.HANDLE_WIDTH &&
               y >= WidgetWrapper.BORDER_N + (getHeight - WidgetWrapper.BORDER_S - WidgetWrapper.BORDER_N -
                                              WidgetWrapper.HANDLE_WIDTH) / 2)
        mouseMode = MouseMode.E
    }
    
    else if (_verticallyResizable && y > getHeight - WidgetWrapper.BORDER_S) {
      if (x <= WidgetWrapper.BORDER_W + (getWidth - WidgetWrapper.BORDER_E - WidgetWrapper.BORDER_W +
                                         WidgetWrapper.HANDLE_WIDTH) / 2 &&
          x >= WidgetWrapper.BORDER_W + (getWidth - WidgetWrapper.BORDER_E - WidgetWrapper.BORDER_W -
                                         WidgetWrapper.HANDLE_WIDTH) / 2)
        mouseMode = MouseMode.S
    }
    
    else if (_verticallyResizable && y < WidgetWrapper.BORDER_N &&
             x <= WidgetWrapper.BORDER_W + (getWidth - WidgetWrapper.BORDER_E - WidgetWrapper.BORDER_W +
                                            WidgetWrapper.HANDLE_WIDTH) / 2 &&
             x >= WidgetWrapper.BORDER_W + (getWidth - WidgetWrapper.BORDER_E - WidgetWrapper.BORDER_W -
                                            WidgetWrapper.HANDLE_WIDTH) / 2)
      mouseMode = MouseMode.N
    
    if (mouseMode == MouseMode.DRAG)
      interfacePanel.aboutToDragSelectedWidgets(startPressX, startPressY)
    else
      aboutToDrag(startPressX, startPressY)
  }

  def aboutToDrag(startX: Int, startY: Int) {
    startPressX = startX
    startPressY = startY
    selected(false, true) // true = change is temporary, don't raise events
    originalBounds = getBounds()
  }

  def mouseDragged(e: MouseEvent) {
    val p = Coordinates.convertPointToScreen(e.getPoint, this)

    if (mouseMode == MouseMode.DRAG) {
      if ((e.getModifiersEx & InputEvent.SHIFT_DOWN_MASK) == 0) {
        constrainToHorizontal = false
        constrainToVertical = false
      }
      
      else {
        if (!constrainToHorizontal && !constrainToVertical &&
            (p.x - startPressX).abs > (p.y - startPressY).abs)
          constrainToHorizontal = true
        else
          constrainToVertical = true

        if (constrainToHorizontal)
          p.y = startPressY
        else if (constrainToVertical)
          p.x = startPressX
      }

      interfacePanel.dragSelectedWidgets(p.x - startPressX, p.y - startPressY)
    }
    
    else if (mouseMode != MouseMode.IDLE)
      doResize(p.x - startPressX, p.y - startPressY)
  }

  def doDrag(x: Int, y: Int) {
    setLocation(x + originalBounds.x, y + originalBounds.y)
  }

  def mouseReleased(e: MouseEvent) {
    if (e.isPopupTrigger && mouseMode != MouseMode.DRAG)
      doPopup(e)
    else if (Mouse.hasButton1(e)) {
      if (mouseMode == MouseMode.DRAG) 
        WidgetActions.moveSelectedWidgets(interfacePanel)
      else if (mouseMode != MouseMode.IDLE)
        WidgetActions.resizeWidget(this)

      mouseMode = MouseMode.IDLE
    }
  }

  def doDrop() {
    selected(true, true) // 2nd true = change was temporary

    new DirtyEvent(None).raise(this)

    getParent.asInstanceOf[WidgetPanel].zoomer.updateZoomInfo(widget)
  }

  private def enforceMinimumSize(r: Rectangle) {
    if (widget != null) {
      var minWidgetSize = widget.getMinimumSize

      getParent match {
        case wp: WidgetPanel => wp.zoomer.zoomSize(minWidgetSize)
        case _ =>
      }

      if (minWidgetSize == null)
        minWidgetSize = new Dimension(WidgetWrapper.MIN_WIDGET_WIDTH, WidgetWrapper.MIN_WIDGET_HEIGHT)

      if (minWidgetSize.width < WidgetWrapper.MIN_WIDGET_WIDTH)
        minWidgetSize.width = WidgetWrapper.MIN_WIDGET_WIDTH

      if (minWidgetSize.height < WidgetWrapper.MIN_WIDGET_HEIGHT)
        minWidgetSize.height = WidgetWrapper.MIN_WIDGET_HEIGHT

      mouseMode match {
        case MouseMode.S =>
          if (r.height < minWidgetSize.height)
            r.height = minWidgetSize.height

        case MouseMode.SW =>
          if (r.width < minWidgetSize.width) {
            r.x -= minWidgetSize.width - r.width
            r.width = minWidgetSize.width
          }

          if (r.height < minWidgetSize.height)
            r.height = minWidgetSize.height

        case MouseMode.SE =>
          if (r.width < minWidgetSize.width)
            r.width = minWidgetSize.width
          
          if (r.height < minWidgetSize.height)
            r.height = minWidgetSize.height

        case MouseMode.E =>
          if (r.width < minWidgetSize.width)
            r.width = minWidgetSize.width

        case MouseMode.NW =>
          if (r.width < minWidgetSize.width) {
            r.x -= minWidgetSize.width - r.width
            r.width = minWidgetSize.width
          }

          if (r.height < minWidgetSize.height) {
            r.y -= minWidgetSize.height - r.height
            r.height = minWidgetSize.height
          }

        case MouseMode.W =>
          if (r.width < minWidgetSize.width) {
            r.x -= minWidgetSize.width - r.width
            r.width = minWidgetSize.width
          }

        case MouseMode.NE =>
          if (r.width < minWidgetSize.width)
            r.width = minWidgetSize.width
          
          if (r.height < minWidgetSize.height) {
            r.y -= minWidgetSize.height - r.height
            r.height = minWidgetSize.height
          }

        case MouseMode.N =>
          if (r.height < minWidgetSize.height) {
            r.y -= minWidgetSize.height - r.height
            r.height = minWidgetSize.height
          }

        case _ => throw new IllegalStateException
      }
    }
  }

  private def enforceMaximumSize(r: Rectangle) {
    if (widget != null) {
      val maxWidgetSize = widget.getMaximumSize

      if (maxWidgetSize == null)
        return
      
      if (maxWidgetSize.height <= 0)
        maxWidgetSize.height = 10000
      
      if (maxWidgetSize.width <= 0)
        maxWidgetSize.width = 10000
      
      getParent match {
        case wp: WidgetPanel => wp.zoomer.zoomSize(maxWidgetSize)
        case _ =>
      }

      mouseMode match {
        case MouseMode.S =>
          if (r.height > maxWidgetSize.height)
            r.height = maxWidgetSize.height
          
        case MouseMode.SW =>
          if (r.width > maxWidgetSize.width)
            r.width = maxWidgetSize.width
          
          if (r.height > maxWidgetSize.height)
            r.height = maxWidgetSize.height
          
        case MouseMode.SE =>
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width
            r.x = getX + getWidth - r.width
          }

          if (r.height > maxWidgetSize.height)
            r.height = maxWidgetSize.height
          
        case MouseMode.E =>
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width
            r.x = getX + getWidth - r.width
          }

        case MouseMode.NW =>
          if (r.width > maxWidgetSize.width)
            r.width = maxWidgetSize.width
          
          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height
            r.y = getY + getHeight - r.height
          }

        case MouseMode.W =>
          if (r.width > maxWidgetSize.width)
            r.width = maxWidgetSize.width
          
        case MouseMode.NE =>
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width
            r.x = getX + getWidth - r.width
          }

          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height
            r.y = getY + getHeight - r.height
          }

        case MouseMode.N =>
          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height
            r.y = getY + getHeight - r.height
          }

        case _ => throw new IllegalStateException
      }
    }
  }

  private def enforceGridSnapSize(r: Rectangle) {
    if (widget != null) {
      val newWidth = (r.width / WidgetPanel.GridSnap) * WidgetPanel.GridSnap
      val newHeight = (r.height / WidgetPanel.GridSnap) * WidgetPanel.GridSnap

      mouseMode match {
        case MouseMode.S =>
          r.height = newHeight

        case MouseMode.SW =>
          r.x -= newWidth - r.width
          r.width = newWidth
          r.height = newHeight

        case MouseMode.SE =>
          r.width = newWidth
          r.height = newHeight

        case MouseMode.E =>
          r.width = newWidth

        case MouseMode.NW =>
          r.x -= newWidth - r.width
          r.y -= newHeight - r.height
          r.width = newWidth
          r.height = newHeight

        case MouseMode.W =>
          r.x -= newWidth - r.width
          r.width = newWidth

        case MouseMode.NE =>
          r.y -= newHeight - r.height
          r.width = newWidth
          r.height = newHeight

        case MouseMode.N =>
          r.y -= newHeight - r.height
          r.height = newHeight

        case _ => throw new IllegalStateException
      }
    }
  }

  def widgetResized() {
    super.setBounds(
      if (selected) {
        new Rectangle(getX, getY, widget.getWidth + WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W,
                      widget.getHeight + WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S)
      }

      else
        new Rectangle(getX, getY, widget.getWidth, widget.getHeight)
    )

    revalidateInterfacePanel()
  }

  def handle(e: WidgetForegroundedEvent) {
    if (e.widget != widget)
      isForeground(false)
  }

  // if we are not selected, return our location if we are selected,
  // return what our location would be if we *weren't* selected... this
  // is needed for the zooming code in InterfacePanel
  def getUnselectedLocation: Point = {
    if (selected)
      new Point(getX + WidgetWrapper.BORDER_E, getY + WidgetWrapper.BORDER_N)
    else
      getLocation
  }

  def getUnselectedBounds: Rectangle = {
    if (selected)
      new Rectangle(getX + WidgetWrapper.BORDER_E, getY + WidgetWrapper.BORDER_N,
                    getWidth - WidgetWrapper.BORDER_E - WidgetWrapper.BORDER_W,
                    getHeight - WidgetWrapper.BORDER_N - WidgetWrapper.BORDER_S)
    else
      getBounds
  }

  ///

  private def doPopup(e: MouseEvent) {
    if (interfacePanel != null) {
      val menu = new WrappingPopupMenu

      menu.setBackground(InterfaceColors.MENU_BACKGROUND)

      val p = populateContextMenu(menu, e.getPoint, e.getSource.asInstanceOf[Component])

      if (menu.getSubElements.size > 0)
        menu.show(e.getSource.asInstanceOf[Component], p.x, p.y)
      
      e.consume()
    }
  }

  private def populateContextMenu(menu: JPopupMenu, p: Point, source: Component): Point = {
    if (widget.getEditable.isInstanceOf[Editable] && !interfacePanel.multiSelected) {
      menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.edit")) {
        def actionPerformed(e: ActionEvent) {
          selected(true)
          foreground()
          new EditWidgetEvent(null).raise(WidgetWrapper.this)
        }
      }))
    }

    if (selected) {
      menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.deselect")) {
        def actionPerformed(e: ActionEvent) {
          selected(false)
          interfacePanel.setForegroundWrapper()
        }
      }))

      if (interfacePanel.multiSelected) {
        menu.add(new JPopupMenu.Separator)

        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignLeft")) {
          def actionPerformed(e: ActionEvent) {
            interfacePanel.alignLeft(WidgetWrapper.this)
          }
        }))

        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignCenterHorizontal")) {
          def actionPerformed(e: ActionEvent) {
            interfacePanel.alignCenterHorizontal(WidgetWrapper.this)
          }
        }))

        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignRight")) {
          def actionPerformed(e: ActionEvent) {
            interfacePanel.alignRight(WidgetWrapper.this)
          }
        }))

        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignTop")) {
          def actionPerformed(e: ActionEvent) {
            interfacePanel.alignTop(WidgetWrapper.this)
          }
        }))

        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignCenterVertical")) {
          def actionPerformed(e: ActionEvent) {
            interfacePanel.alignCenterVertical(WidgetWrapper.this)
          }
        }))

        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignBottom")) {
          def actionPerformed(e: ActionEvent) {
            interfacePanel.alignBottom(WidgetWrapper.this)
          }
        }))

        menu.add(new JPopupMenu.Separator)

        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.distributeHorizontal")) {
          def actionPerformed(e: ActionEvent) {
            interfacePanel.distributeHorizontal()
          }
        }))

        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.distributeVertical")) {
          def actionPerformed(e: ActionEvent) {
            interfacePanel.distributeVertical()
          }
        }))
      }
    }
    
    else {
      menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.select")) {
        def actionPerformed(e: ActionEvent) {
          selected(true)
          foreground()
        }
      }))
    }

    if (interfacePanel.selectedWrappers.size > 1) {
      menu.add(new JPopupMenu.Separator)

      menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.deleteSelected")) {
        def actionPerformed(e: ActionEvent) {
          interfacePanel.deleteSelectedWidgets()
        }
      }))
    }

    else if (widget.deleteable) {
      menu.add(new JPopupMenu.Separator)

      menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.delete")) {
        def actionPerformed(e: ActionEvent) {
          WidgetActions.removeWidget(interfacePanel, WidgetWrapper.this)
        }
      }))
    }

    if (widget.hasContextMenu) {
      menu.add(new JPopupMenu.Separator)

      val location = widget.populateContextMenu(menu, p, source)

      if (widget.exportable) {
        menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.export")) {
          def actionPerformed(e: ActionEvent) {
            new ExportWidgetEvent(widget).raise(WidgetWrapper.this)
          }
        }))
      }

      widget.addExtraMenuItems(menu)

      return location
    }

    p
  }

  def syncTheme() {
    shadowPane.syncTheme()
    widget.syncTheme()
  }
}
