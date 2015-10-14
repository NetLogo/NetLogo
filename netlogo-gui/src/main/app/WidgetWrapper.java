// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import org.nlogo.window.MouseMode;
import org.nlogo.window.Widget;

import java.awt.Dimension;

// public for widget extension - ST 6/12/08
public strictfp class WidgetWrapper
    extends javax.swing.JLayeredPane
    implements
    org.nlogo.window.WidgetWrapperInterface,
    java.awt.event.MouseListener,
    java.awt.event.MouseMotionListener,
    org.nlogo.window.Events.WidgetForegroundedEvent.Handler {

  // fudge factor to account for different platforms having different font sizes;
  // eventually we should deal with this in a more sophisticated way
  private static final double PREFERRED_WIDTH_FUDGE_FACTOR = 0.15;

  private boolean verticallyResizable;

  public boolean verticallyResizable() {
    return verticallyResizable;
  }

  private boolean horizontallyResizable;

  public boolean horizontallyResizable() {
    return horizontallyResizable;
  }

  private boolean isNew = false;

  public boolean isNew() {
    return isNew;
  }

  void isNew(boolean isNew) {
    this.isNew = isNew;
  }

  WidgetWrapper(Widget widget, WidgetPanel interfacePanel) {
    this.widget = widget;
    this.interfacePanel = interfacePanel;
    setOpaque(true);
    verticallyResizable = computeVerticallyResizable();
    horizontallyResizable = computeHorizontallyResizable();
    topBar = new WindowBar(WindowBar.Type.TOP, BORDER_E, BORDER_W, verticallyResizable);
    leftBar = new WindowBar(WindowBar.Type.SIDE, 0, 0, horizontallyResizable);
    rightBar = new WindowBar(WindowBar.Type.SIDE, 0, 0, horizontallyResizable);
    bottomBar = new WindowBar(WindowBar.Type.BOTTOM, BORDER_E, BORDER_W, verticallyResizable);
    widgetChanged(); // update cornerHandles and otherwise make sure we are in good state -- CLB

    // don't let mouse events get through to the InterfacePanel
    // (is there a more elegant way to do this?) - ST 8/9/03
    addMouseListener(new java.awt.event.MouseAdapter() {
    });
    addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
    });

    glass = new javax.swing.JComponent() {
    };
    glass.setOpaque(false);
    glass.addMouseListener(this);
    glass.addMouseMotionListener(this);

    widget.addPopupListeners(popupListener);

    setCursor(java.awt.Cursor.getDefaultCursor());
    setLayout(null);

    setBackground(widget.getBackground());
    // this is for notes, when the bg is transparent we don't want to see the
    // white widget wrapper.  I don't know why setting the background to a
    // transparent color doesn't work.  but it doesn't ev 6/8/07
    setOpaque(widget.widgetWrapperOpaque());

    add(glass, javax.swing.JLayeredPane.DRAG_LAYER);
    add(widget);
    add(topBar);
    add(leftBar);
    add(rightBar);
    add(bottomBar);
    doLayout();
  }

  public boolean computeVerticallyResizable() {
    return widget.getMaximumSize() == null ||
        widget.getMaximumSize().height != widget.getMinimumSize().height;
  }

  public boolean computeHorizontallyResizable() {
    return widget.getMaximumSize() == null ||
        widget.getMaximumSize().width != widget.getMinimumSize().width;
  }

  public void widgetChanged() {
    verticallyResizable = computeVerticallyResizable();
    horizontallyResizable = computeHorizontallyResizable();
    if (verticallyResizable()) {
      topBar.handles(true);
      bottomBar.handles(true);
    } else {
      topBar.handles(false);
      bottomBar.handles(false);
    }

    if (horizontallyResizable()) {
      topBar.cornerHandles(true);
      bottomBar.cornerHandles(true);
      leftBar.handles(true);
      rightBar.handles(true);
    } else {
      topBar.cornerHandles(false);
      bottomBar.cornerHandles(false);
      leftBar.handles(false);
      rightBar.handles(false);
    }
  }

  private final java.awt.event.MouseListener popupListener =
      new java.awt.event.MouseAdapter() {
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
          if (e.isPopupTrigger()) {
            doPopup(e);
          }
        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
          if (e.isPopupTrigger()) {
            doPopup(e);
          }
        }
      };

  @Override
  public boolean isValidateRoot() {
    return true;
  }

  private boolean selected = false;

  public boolean selected() {
    return selected;
  }

  void selected(boolean selected) {
    selected(selected, false);
  }

  void selected(boolean selected, boolean temporary) {
    boolean changed = this.selected != selected;

    if (changed) {
      this.selected = selected;
      java.awt.Rectangle bounds = getBounds();
      if (selected) {
        bounds.x -= BORDER_E;
        bounds.width += BORDER_E + BORDER_W;
        bounds.y -= BORDER_N;
        bounds.height += BORDER_N + BORDER_S;
      } else {
        isForeground(false);
        bounds.x += BORDER_E;
        bounds.width -= BORDER_E + BORDER_W;
        bounds.y += BORDER_N;
        bounds.height -= BORDER_N + BORDER_S;
      }

      setBounds(bounds);
      if (!temporary) {
        new Events.WidgetSelectedEvent(widget, selected)
            .raise(this);
      }
    }
  }

  private void revalidateInterfacePanel() {
    if (interfacePanel() != null) {
      interfacePanel().revalidate();
    }
  }

  @Override
  public void setBounds(java.awt.Rectangle r) {
    setBounds(r.x, r.y, r.width, r.height);
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    boolean sizeChanged = getWidth() != width || getHeight() != height;
    super.setBounds(x, y, width, height);
    if (sizeChanged) {
      doLayout();
      revalidateInterfacePanel();
    }
  }

  private boolean isForeground = false;

  boolean isForeground() {
    return isForeground;
  }

  private static final java.awt.Color NON_FOREGROUND_BACKGROUND =
      new java.awt.Color(205, 205, 205);

  void isForeground(boolean isForeground) {
    this.isForeground = isForeground;
    if (isForeground) {
      topBar.setBackground(java.awt.Color.GRAY);
      leftBar.setBackground(java.awt.Color.GRAY);
      rightBar.setBackground(java.awt.Color.GRAY);
      bottomBar.setBackground(java.awt.Color.GRAY);

      topBar.setForeground(java.awt.Color.BLACK);
      leftBar.setForeground(java.awt.Color.BLACK);
      rightBar.setForeground(java.awt.Color.BLACK);
      bottomBar.setForeground(java.awt.Color.BLACK);
    } else {
      topBar.setBackground(NON_FOREGROUND_BACKGROUND);
      leftBar.setBackground(NON_FOREGROUND_BACKGROUND);
      rightBar.setBackground(NON_FOREGROUND_BACKGROUND);
      bottomBar.setBackground(NON_FOREGROUND_BACKGROUND);

      topBar.setForeground(java.awt.Color.GRAY);
      leftBar.setForeground(java.awt.Color.GRAY);
      rightBar.setForeground(java.awt.Color.GRAY);
      bottomBar.setForeground(java.awt.Color.GRAY);
    }

    topBar.repaint();
    leftBar.repaint();
    rightBar.repaint();
    bottomBar.repaint();
  }

  void foreground() {
    if (!isForeground()) {
      isForeground(true);
      new org.nlogo.window.Events.WidgetForegroundedEvent(widget)
          .raise(this);
    }
  }

  private java.awt.Dimension addWrapperBorder(Dimension dim) {
    // some widgets have no max size.
    // Adding the border dimensions to that results in another null -- CLB
    if (dim == null) {
      return null;
    }

    if (widget.needsPreferredWidthFudgeFactor()) {
      // kludge city...
      dim.width = (int) (dim.width * (1 + PREFERRED_WIDTH_FUDGE_FACTOR));
    }

    if (getParent() instanceof WidgetPanel && !widget.isNote()) {
      WidgetPanel iP = (WidgetPanel) getParent();
      dim = iP.zoomer().zoomSize(dim);
    }
    if (selected()) {
      dim.width += BORDER_E + BORDER_W;
      dim.height += BORDER_N + BORDER_S;
    }
    return dim;
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    java.awt.Dimension dim =
        new java.awt.Dimension(widget.getUnzoomedPreferredSize());
    if (widget.isNote()) {
      dim = new java.awt.Dimension(widget.getPreferredSize());
    }
    return addWrapperBorder(dim);
  }

  @Override
  public Dimension getMaximumSize() {
    return addWrapperBorder(widget.getMaximumSize());
  }


  @Override
  public void doLayout() {
    // make sure opacity gets set properly since it might
    // have changed. ev 8/8/07
    setOpaque(widget.widgetWrapperOpaque());
    if (selected()) {
      topBar.setVisible(true);
      leftBar.setVisible(true);
      rightBar.setVisible(true);
      bottomBar.setVisible(true);
      topBar.setBounds(0, 0, getWidth(), BORDER_N);
      widget.setBounds(BORDER_E,
          BORDER_N,
          getWidth() - (BORDER_E + BORDER_W),
          getHeight() - (BORDER_N + BORDER_S));
      leftBar.setBounds(0,
          BORDER_N,
          BORDER_E,
          getHeight() - BORDER_N - BORDER_S);
      rightBar.setBounds(getWidth() - BORDER_W,
          BORDER_N,
          BORDER_W,
          getHeight() - BORDER_N - BORDER_S);
      bottomBar.setBounds(0,
          getHeight() - BORDER_S,
          getWidth(),
          BORDER_S);
    } else {
      topBar.setVisible(false);
      leftBar.setVisible(false);
      rightBar.setVisible(false);
      bottomBar.setVisible(false);
      topBar.setBounds(0, 0, 0, 0);
      leftBar.setBounds(0, 0, 0, 0);
      rightBar.setBounds(0, 0, 0, 0);
      bottomBar.setBounds(0, 0, 0, 0);
      widget.setBounds(0, 0, getWidth(), getHeight());
    }
    glass.setBounds(0, 0, getWidth(), getHeight());
    glass.setVisible(selected());
  }

  private final Widget widget;

  public Widget widget() {
    return widget;
  }

  private final WidgetPanel interfacePanel;

  private WidgetPanel interfacePanel() {
    return interfacePanel;
  }

  private final javax.swing.JComponent glass;
  private final WindowBar topBar;
  private final WindowBar leftBar;
  private final WindowBar rightBar;
  private final WindowBar bottomBar;

  static final int BORDER_N = 10;
  static final int BORDER_S = 9;
  static final int BORDER_E = 9;
  static final int BORDER_W = 9;

  static final int HANDLE_WIDTH = 9;

  private static final int MIN_WIDGET_WIDTH = 12;
  private static final int MIN_WIDGET_HEIGHT = 12;

  private MouseMode mouseMode = MouseMode.IDLE;

  private MouseMode mouseMode() {
    return mouseMode;
  }

  private void mouseMode(MouseMode mouseMode) {
    this.mouseMode = mouseMode;
    /*
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
      */
  }

  void doResize(int x, int y) {
    java.awt.Rectangle bounds = new java.awt.Rectangle(originalBounds);
    switch (mouseMode()) {
      case NW:
        y = StrictMath.max(y, BORDER_N - bounds.y);
        bounds.x += x;
        bounds.width -= x;
        bounds.y += y;
        bounds.height -= y;
        break;
      case NE:
        y = StrictMath.max(y, BORDER_N - bounds.y);
        bounds.width += x;
        bounds.y += y;
        bounds.height -= y;
        break;
      case SW:
        bounds.x += x;
        bounds.width -= x;
        bounds.height += y;
        break;
      case W:
        bounds.x += x;
        bounds.width -= x;
        break;
      case SE:
        bounds.width += x;
        bounds.height += y;
        break;
      case E:
        bounds.width += x;
        break;
      case S:
        bounds.height += y;
        break;
      case N:
        y = StrictMath.max(y, BORDER_N - bounds.y);
        bounds.y += y;
        bounds.height -= y;
        break;
      default:
        throw new IllegalStateException();
    }
    if (interfacePanel.workspace.snapOn() && !interfacePanel.isZoomed()) {
      enforceGridSnapSize(bounds);
    }
    enforceMinimumSize(bounds);
    enforceMaximumSize(bounds);
    setBounds(widget.constrainDrag(bounds, originalBounds, mouseMode()));
  }

  public int gridSnap() {
    return interfacePanel.workspace.snapOn() ? WidgetPanel.GRID_SNAP : 1;
  }

  public void mouseMoved(java.awt.event.MouseEvent e) {
  }

  public void mouseClicked(java.awt.event.MouseEvent e) {
  }

  public void mouseEntered(java.awt.event.MouseEvent e) {
  }

  public void mouseExited(java.awt.event.MouseEvent e) {
  }

  public int startPressX;
  public int startPressY;

  public void mousePressed(java.awt.event.MouseEvent e) {
    if (e.isPopupTrigger()) {
      doPopup(e);
      return;
    }
    if (!org.nlogo.awt.Mouse.hasButton1(e)) {
      return;
    }
    foreground();
    if (e.getClickCount() == 2) {
      new org.nlogo.window.Events.EditWidgetEvent(null).raise(this);
      return;
    }
    java.awt.Dimension d = getSize();
    int x = e.getX();
    int y = e.getY();
    java.awt.Point p =
      org.nlogo.awt.Coordinates.convertPointToScreen(e.getPoint(), this);
    startPressX = p.x;
    startPressY = p.y;

    mouseMode(MouseMode.DRAG);
    if (x < BORDER_W) {
      if (verticallyResizable && y < BORDER_N) {
        mouseMode(MouseMode.NW);
      } else if (verticallyResizable && y > d.height - BORDER_S) {
        mouseMode(MouseMode.SW);
      } else if (y <= BORDER_N + (d.height - BORDER_S - BORDER_N - HANDLE_WIDTH) / 2 + HANDLE_WIDTH &&
          y >= BORDER_N + (d.height - BORDER_S - BORDER_N - HANDLE_WIDTH) / 2) {
        mouseMode(MouseMode.W);
      }
    } else if (x > d.width - BORDER_E) {
      if (verticallyResizable && y < BORDER_N) {
        mouseMode(MouseMode.NE);
      } else if (verticallyResizable && y > d.height - BORDER_S) {
        mouseMode(MouseMode.SE);
      } else if (y <= BORDER_N + (d.height - BORDER_S - BORDER_N - HANDLE_WIDTH) / 2 + HANDLE_WIDTH &&
          y >= BORDER_N + (d.height - BORDER_S - BORDER_N - HANDLE_WIDTH) / 2) {
        mouseMode(MouseMode.E);
      }
    } else if (verticallyResizable && y > d.height - BORDER_S) {
      if (x <= BORDER_W + (d.width - BORDER_E - BORDER_W + HANDLE_WIDTH) / 2 &&
          x >= BORDER_W + (d.width - BORDER_E - BORDER_W - HANDLE_WIDTH) / 2) {
        mouseMode(MouseMode.S);
      }
    } else if (verticallyResizable && y < BORDER_N &&
        x <= BORDER_W + (d.width - BORDER_E - BORDER_W + HANDLE_WIDTH) / 2 &&
        x >= BORDER_W + (d.width - BORDER_E - BORDER_W - HANDLE_WIDTH) / 2) {
      mouseMode(MouseMode.N);
    }
    if (mouseMode() == MouseMode.DRAG) {
      interfacePanel().aboutToDragSelectedWidgets(startPressX, startPressY);
    } else {
      aboutToDrag();
    }
  }

  public java.awt.Rectangle originalBounds;

  void aboutToDrag() {
    selected(false, true); // true = change is temporary, don't raise events
    originalBounds = getBounds();
  }

  private boolean constrainToHorizontal = false;
  private boolean constrainToVertical = false;

  public void mouseDragged(java.awt.event.MouseEvent e) {
    java.awt.Point p =
      org.nlogo.awt.Coordinates.convertPointToScreen(e.getPoint(), this);
    int x = p.x;
    int y = p.y;
    if (mouseMode() == MouseMode.DRAG) {
      if ((e.getModifiers() & java.awt.event.InputEvent.SHIFT_MASK) == 0) {
        constrainToHorizontal = false;
        constrainToVertical = false;
      } else {
        if (!constrainToHorizontal && !constrainToVertical &&
            StrictMath.abs(x - startPressX) >
                StrictMath.abs(y - startPressY)) {
          constrainToHorizontal = true;
        } else {
          constrainToVertical = true;
        }
        if (constrainToHorizontal) {
          y = startPressY;
        } else if (constrainToVertical) {
          x = startPressX;
        }
      }
      interfacePanel().dragSelectedWidgets(x - startPressX, y - startPressY);
    } else if (mouseMode() == MouseMode.NE || mouseMode() == MouseMode.NW
        || mouseMode() == MouseMode.SE || mouseMode() == MouseMode.SW
        || mouseMode() == MouseMode.S || mouseMode() == MouseMode.W
        || mouseMode() == MouseMode.E || mouseMode() == MouseMode.N) {
      doResize(x - startPressX, y - startPressY);
    }
  }

  void doDrag(int x, int y) {
    setLocation(x + originalBounds.x, y + originalBounds.y);
  }

  public void mouseReleased(java.awt.event.MouseEvent e) {
    if (e.isPopupTrigger()) {
      doPopup(e);
      return;
    } else if (org.nlogo.awt.Mouse.hasButton1(e)) {
      if (mouseMode() == MouseMode.DRAG) {
        interfacePanel().dropSelectedWidgets();
      } else if (mouseMode() == MouseMode.NE || mouseMode() == MouseMode.NW
          || mouseMode() == MouseMode.SE || mouseMode() == MouseMode.SW
          || mouseMode() == MouseMode.S || mouseMode() == MouseMode.W
          || mouseMode() == MouseMode.E || mouseMode() == MouseMode.N) {
        doDrop();
      }
      mouseMode(MouseMode.IDLE);
    }
  }

  void doDrop() {
    selected(true, true); // 2nd true = change was temporary
    new org.nlogo.window.Events.DirtyEvent().raise(this);
    ((WidgetPanel) getParent()).zoomer().updateZoomInfo(widget);
  }

  private void enforceMinimumSize(java.awt.Rectangle r) {
    if (widget() != null) {
      java.awt.Dimension minWidgetSize = widget().getMinimumSize();
      if (getParent() instanceof WidgetPanel) {
        WidgetPanel iP = (WidgetPanel) getParent();
        minWidgetSize = iP.zoomer().zoomSize(minWidgetSize);
      }
      if (minWidgetSize == null) {
        minWidgetSize = new java.awt.Dimension(MIN_WIDGET_WIDTH, MIN_WIDGET_HEIGHT);
      }
      if (minWidgetSize.width < MIN_WIDGET_WIDTH) {
        minWidgetSize.width = MIN_WIDGET_WIDTH;
      }
      if (minWidgetSize.height < MIN_WIDGET_HEIGHT) {
        minWidgetSize.height = MIN_WIDGET_HEIGHT;
      }

      switch (mouseMode()) {
        case S:
          if (r.height < minWidgetSize.height) {
            r.height = minWidgetSize.height;
          }
          break;
        case SW:
          if (r.width < minWidgetSize.width) {
            r.x -= minWidgetSize.width - r.width;
            r.width = minWidgetSize.width;
          }
          if (r.height < minWidgetSize.height) {
            r.height = minWidgetSize.height;
          }
          break;
        case SE:
          if (r.width < minWidgetSize.width) {
            r.width = minWidgetSize.width;
          }
          if (r.height < minWidgetSize.height) {
            r.height = minWidgetSize.height;
          }
          break;
        case E:
          if (r.width < minWidgetSize.width) {
            r.width = minWidgetSize.width;
          }
          break;
        case NW:
          if (r.width < minWidgetSize.width) {
            r.x -= minWidgetSize.width - r.width;
            r.width = minWidgetSize.width;
          }
          if (r.height < minWidgetSize.height) {
            r.y -= minWidgetSize.height - r.height;
            r.height = minWidgetSize.height;
          }
          break;
        case W:
          if (r.width < minWidgetSize.width) {
            r.x -= minWidgetSize.width - r.width;
            r.width = minWidgetSize.width;
          }
          break;
        case NE:
          if (r.width < minWidgetSize.width) {
            r.width = minWidgetSize.width;
          }
          if (r.height < minWidgetSize.height) {
            r.y -= minWidgetSize.height - r.height;
            r.height = minWidgetSize.height;
          }
          break;
        case N:
          if (r.height < minWidgetSize.height) {
            r.y -= minWidgetSize.height - r.height;
            r.height = minWidgetSize.height;
          }
          break;
        default:
          throw new IllegalStateException();
      }
    }
  }

  private void enforceMaximumSize(java.awt.Rectangle r) {
    if (widget() != null) {
      java.awt.Dimension maxWidgetSize = widget().getMaximumSize();
      if (maxWidgetSize == null) {
        return;
      }
      if (maxWidgetSize.height <= 0) {
        maxWidgetSize.height = 10000;
      }
      if (maxWidgetSize.width <= 0) {
        maxWidgetSize.width = 10000;
      }
      if (getParent() instanceof WidgetPanel) {
        WidgetPanel iP = (WidgetPanel) getParent();
        maxWidgetSize = iP.zoomer().zoomSize(maxWidgetSize);
      }

      switch (mouseMode()) {
        case S:
          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height;
          }
          break;
        case SW:
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width;
          }
          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height;
          }
          break;
        case SE:
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width;
            r.x = getBounds().x + getBounds().width - r.width;
          }
          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height;
          }
          break;
        case E:
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width;
            r.x = getBounds().x + getBounds().width - r.width;
          }
          break;
        case NW:
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width;
          }
          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height;
            r.y = getBounds().y + getBounds().height - r.height;
          }
          break;
        case W:
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width;
          }
          break;
        case NE:
          if (r.width > maxWidgetSize.width) {
            r.width = maxWidgetSize.width;
            r.x = getBounds().x + getBounds().width - r.width;
          }
          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height;
            r.y = getBounds().y + getBounds().height - r.height;
          }
          break;
        case N:
          if (r.height > maxWidgetSize.height) {
            r.height = maxWidgetSize.height;
            r.y = getBounds().y + getBounds().height - r.height;
          }
          break;
        default:
          throw new IllegalStateException();
      }
    }
  }

  private void enforceGridSnapSize(java.awt.Rectangle r) {
    if (widget() != null) {
      int newWidth = (r.width / WidgetPanel.GRID_SNAP) * WidgetPanel.GRID_SNAP;
      int newHeight = (r.height / WidgetPanel.GRID_SNAP) * WidgetPanel.GRID_SNAP;

      switch (mouseMode()) {
        case S:
          r.height = newHeight;
          break;
        case SW:
          r.x -= newWidth - r.width;
          r.width = newWidth;
          r.height = newHeight;
          break;
        case SE:
          r.width = newWidth;
          r.height = newHeight;
          break;
        case E:
          r.width = newWidth;
          break;
        case NW:
          r.x -= newWidth - r.width;
          r.y -= newHeight - r.height;
          r.width = newWidth;
          r.height = newHeight;
          break;
        case W:
          r.x -= newWidth - r.width;
          r.width = newWidth;
          break;
        case NE:
          r.y -= newHeight - r.height;
          r.width = newWidth;
          r.height = newHeight;
          break;
        case N:
          r.y -= newHeight - r.height;
          r.height = newHeight;
          break;
        default:
          throw new IllegalStateException();
      }
    }
  }

  void widgetResized() {
    java.awt.Rectangle rect = getBounds();
    rect.width = widget().getWidth();
    rect.height = widget().getHeight();

    if (selected()) {
      rect.width += BORDER_E + BORDER_W;
      rect.height += BORDER_N + BORDER_S;
    }
    super.setBounds(rect);
    revalidateInterfacePanel();
  }


  public void handle(org.nlogo.window.Events.WidgetForegroundedEvent e) {
    if (!e.widget.equals(widget)) {
      isForeground(false);
    }
  }

  // if we are not selected, return our location if we are selected,
  // return what our location would be if we *weren't* selected... this
  // is needed for the zooming code in InterfacePanel

  public java.awt.Point getUnselectedLocation() {
    java.awt.Point result = getLocation();
    if (selected()) {
      result.x += BORDER_E;
      result.y += BORDER_N;
    }
    return result;
  }

  java.awt.Rectangle getUnselectedBounds() {
    java.awt.Rectangle result = getBounds();
    if (selected()) {
      result.x += BORDER_E;
      result.y += BORDER_N;
      result.width -= BORDER_E + BORDER_W;
      result.height -= BORDER_N + BORDER_S;
    }
    return result;
  }

  ///

  private void doPopup(java.awt.event.MouseEvent e) {
    if (interfacePanel() != null) {
      javax.swing.JPopupMenu menu = new org.nlogo.swing.WrappingPopupMenu();
      java.awt.Point p = e.getPoint();
      p = populateContextMenu(menu, p, (java.awt.Component) e.getSource());
      if (menu.getSubElements().length > 0) {
        menu.show((java.awt.Component) e.getSource(), p.x, p.y);
      }
      e.consume();
    }
  }

  private java.awt.Point populateContextMenu(javax.swing.JPopupMenu menu, java.awt.Point p,
                                             java.awt.Component source) {
    java.awt.Point location = p;

    if (widget().getEditable() instanceof org.nlogo.api.Editable) {
      javax.swing.JMenuItem editItem =
          new javax.swing.JMenuItem("Edit...");
      editItem.addActionListener
          (new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              selected(true);
              foreground();
              new org.nlogo.window.Events.EditWidgetEvent(null)
                  .raise(WidgetWrapper.this);
            }
          });
      menu.add(editItem);
    }

    if (selected()) {
      javax.swing.JMenuItem unselectItem =
          new javax.swing.JMenuItem("Unselect");
      unselectItem.addActionListener
          (new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              selected(false);
              interfacePanel().setForegroundWrapper();
            }
          });
      menu.add(unselectItem);
    } else {
      javax.swing.JMenuItem selectItem =
          new javax.swing.JMenuItem("Select");
      selectItem.addActionListener
          (new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              selected(true);
              foreground();
            }
          });
      menu.add(selectItem);
    }
    if (widget().deleteable()) {
      javax.swing.JMenuItem deleteItem =
          new javax.swing.JMenuItem("Delete");
      deleteItem.addActionListener
          (new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              interfacePanel().deleteWidget(WidgetWrapper.this);
            }
          });
      menu.add(new javax.swing.JPopupMenu.Separator());
      menu.add(deleteItem);
    }
    if (widget().hasContextMenu()) {
      menu.add(new javax.swing.JPopupMenu.Separator());
      location = widget().populateContextMenu(menu, p, source);
      if (widget().exportable()) {
        javax.swing.JMenuItem exportItem =
            new javax.swing.JMenuItem("Export...");
        exportItem.addActionListener
            (new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                  String exportPath = org.nlogo.swing.FileDialog.show
                      (widget(), "Export",
                          java.awt.FileDialog.SAVE,
                          interfacePanel.workspace().guessExportName
                              (widget.getDefaultExportName()));
                  widget().export(exportPath);
                } catch (org.nlogo.awt.UserCancelException uce) {
                  org.nlogo.util.Exceptions.ignore(uce);
                }
              }
            });
        menu.add(exportItem);
      }

      widget.addExtraMenuItems(menu);
    }
    return location;
  }

}
