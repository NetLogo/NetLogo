// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import org.nlogo.api.I18N;
import org.nlogo.window.DummyPlotWidget;
import org.nlogo.window.EditorColorizer;
import org.nlogo.window.GUIWorkspace;
import org.nlogo.window.Widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// note that an instance of this class is used for the hubnet client editor
// and its subclass InterfacePanel is used for the interface tab.
// there are a few things in here that are specific to the client editor behavior
// (eg the way it handles plots) which is overridden in the subclass. ev 1/25/07

// public for widget extension - ST 6/12/08
public strictfp class WidgetPanel
    extends org.nlogo.window.AbstractWidgetPanel
    implements
    org.nlogo.window.WidgetContainer,
    java.awt.event.MouseListener,
    java.awt.event.MouseMotionListener,
    java.awt.event.FocusListener,
    org.nlogo.window.Events.WidgetEditedEventHandler,
    org.nlogo.window.Events.WidgetRemovedEventHandler,
    org.nlogo.window.Events.LoadBeginEventHandler {
  static final int GRID_SNAP = 5;  // set the size of the grid, in pixels

  protected java.awt.Rectangle selectionRect;
  protected java.awt.Point startDragPoint;
  protected WidgetWrapper newWidget;
  protected List<WidgetWrapper> widgetsBeingDragged;
  protected final GUIWorkspace workspace;
  protected final javax.swing.JComponent glassPane =
      new javax.swing.JComponent() {
        @Override
        public void paintComponent(java.awt.Graphics g) {
          if (selectionRect != null) {
            g.setColor(java.awt.Color.WHITE);
            g.drawRect(selectionRect.x, selectionRect.y,
                selectionRect.width - 1, selectionRect.height - 1);
            g.setColor(new java.awt.Color(180, 180, 180, 120));
            g.fillRect(selectionRect.x, selectionRect.y,
                selectionRect.width - 1, selectionRect.height - 1);
          }
        }
      };

  public WidgetPanel(GUIWorkspace workspace) {
    this.workspace = workspace;
    setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
    setOpaque(true);
    setBackground(java.awt.Color.WHITE);
    addMouseListener(this);
    addMouseMotionListener(this);
    addFocusListener(this);
    setAutoscrolls(true);
    glassPane.setOpaque(false);
    glassPane.setVisible(false);
    add(glassPane, DRAG_LAYER);
  }

  @Override
  public boolean isOptimizedDrawingEnabled() {
    return false; // our children may overlap
  }

  @Override
  public void requestFocus() {
    requestFocusInWindow();
  }

  protected boolean hasFocus = false;

  public void focusGained(java.awt.event.FocusEvent e) {
    hasFocus = true;
  }

  public void focusLost(java.awt.event.FocusEvent e) {
    hasFocus = false;
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(0, 0);
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    return getPreferredSize(false);
  }

  org.nlogo.window.GUIWorkspace workspace() {
    return workspace;
  }

  @Override
  public boolean empty() {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        return false;
      }
    }
    return true;
  }

  public java.awt.Dimension getPreferredSize(boolean savingAsApplet) {
    int maxX = 0, maxY = 0;
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] == glassPane) {
        continue;
      }
      java.awt.Point location = comps[i].getLocation();
      java.awt.Dimension size = comps[i].getSize();
      int x = location.x + size.width;
      int y = location.y + size.height;
      if (!savingAsApplet &&
          comps[i] instanceof WidgetWrapper &&
          !((WidgetWrapper) comps[i]).selected()) {
        x += WidgetWrapper.BORDER_E;
        y += WidgetWrapper.BORDER_S;
      }
      if (x > maxX) {
        maxX = x;
      }
      if (y > maxY) {
        maxY = y;
      }
    }
    if (!savingAsApplet &&
        System.getProperty("os.name").startsWith("Mac") &&
        System.getProperty("os.version").startsWith("10.2")) {
      // allow for the intrusion of the window grow box into the
      // lower right corner
      maxX += 8;
      maxY += 8;
    }
    return new java.awt.Dimension(maxX, maxY);
  }

  ///

  org.nlogo.window.OutputWidget getOutputWidget() {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        WidgetWrapper wrapper = (WidgetWrapper) comps[i];
        if (wrapper.widget() instanceof org.nlogo.window.OutputWidget) {
          return (org.nlogo.window.OutputWidget) wrapper.widget();
        }
      }
    }
    return null;
  }

  WidgetCreator widgetCreator = null;

  public void setWidgetCreator(WidgetCreator widgetCreator) {
    this.widgetCreator = widgetCreator;
  }

  ///

  public WidgetWrapper getWrapper(Widget widget) {
    return (WidgetWrapper) widget.getParent();
  }

  protected List<WidgetWrapper> selectedWrappers() {
    List<WidgetWrapper> result =
        new ArrayList<WidgetWrapper>();
    java.awt.Component[] comps = getComponents();
    for (int i = 0; comps.length != i; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        WidgetWrapper wrapper = (WidgetWrapper) comps[i];
        if (wrapper.selected()) {
          result.add(wrapper);
        }
      }
    }
    return result;
  }

  void aboutToDragSelectedWidgets(int startPressX, int startPressY) {
    widgetsBeingDragged = new ArrayList<WidgetWrapper>();
    List<WidgetWrapper> selectedWrappers = selectedWrappers();
    for (int i = 0; i < selectedWrappers.size(); i++) {
      WidgetWrapper w = selectedWrappers.get(i);
      w.startPressX = startPressX;
      w.startPressY = startPressY;
      w.aboutToDrag();
      widgetsBeingDragged.add(w);
    }
  }

  void dragSelectedWidgets(int x, int y) {
    java.awt.Point p = new java.awt.Point(x, y);
    for (int i = 0; i < widgetsBeingDragged.size(); i++) {
      WidgetWrapper w = widgetsBeingDragged.get(i);
      p = restrictDrag(p, w);
    }

    for (int i = 0; i < widgetsBeingDragged.size(); i++) {
      WidgetWrapper w = widgetsBeingDragged.get(i);
      w.doDrag(p.x, p.y);
    }
  }

  protected java.awt.Point restrictDrag(java.awt.Point p, WidgetWrapper w) {
    int x = p.x;
    int y = p.y;
    java.awt.Rectangle wb = w.originalBounds;
    java.awt.Rectangle b = getBounds();
    java.awt.Rectangle newWb =
        new java.awt.Rectangle(wb.x + x, wb.y + y, wb.width, wb.height);
    if (workspace.snapOn() && !this.isZoomed()) {
      int xGridSnap = newWb.x - (newWb.x / GRID_SNAP) * GRID_SNAP;
      int yGridSnap = newWb.y - (newWb.y / GRID_SNAP) * GRID_SNAP;
      x -= xGridSnap;
      y -= yGridSnap;
      newWb.x -= xGridSnap;
      newWb.y -= yGridSnap;
    }

    if (newWb.x + newWb.width < WidgetWrapper.BORDER_E * 2) {
      x += WidgetWrapper.BORDER_E * 2 - (newWb.x + newWb.width);
    }
    if (newWb.y < WidgetWrapper.BORDER_N) {
      y += WidgetWrapper.BORDER_N - newWb.y;
    }
    if (newWb.x + 2 * WidgetWrapper.BORDER_W > b.width) {
      x -= (newWb.x + 2 * WidgetWrapper.BORDER_W) - b.width;
    }
    if (newWb.y + WidgetWrapper.BORDER_N > b.height) {
      y -= (newWb.y + WidgetWrapper.BORDER_N) - b.height;
    }

    return new java.awt.Point(x, y);
  }

  void dropSelectedWidgets() {
    for (int i = 0; i < widgetsBeingDragged.size(); i++) {
      WidgetWrapper w = widgetsBeingDragged.get(i);
      w.doDrop();
    }
    widgetsBeingDragged = null;
    setForegroundWrapper();
  }

  public void mouseMoved(java.awt.event.MouseEvent e) {
  }

  public void mouseDragged(java.awt.event.MouseEvent e) {
    if (!org.nlogo.awt.Mouse.hasButton1(e)) {
      return;
    }
    java.awt.Point p = e.getPoint();
    java.awt.Rectangle rect = this.getBounds();

    p.x += rect.x;
    p.y += rect.y;

    if (newWidget != null) {
      if (workspace.snapOn()) {
        startDragPoint.x = (startDragPoint.x / GRID_SNAP) * GRID_SNAP;
        startDragPoint.y = (startDragPoint.y / GRID_SNAP) * GRID_SNAP;
      }
      java.awt.Point p2 =
          restrictDrag(new java.awt.Point(e.getX() - startDragPoint.x,
              e.getY() - startDragPoint.y),
              newWidget);
      newWidget.setLocation(startDragPoint.x + p2.x, startDragPoint.y + p2.y);
    } else if (null != startDragPoint) {
      if (!glassPane.isVisible()) {
        glassPane.setBounds(0, 0, getWidth(), getHeight());
        glassPane.setVisible(true);
      }
      scrollRectToVisible
          (new java.awt.Rectangle
              (e.getX() - 20, e.getY() - 20, 40, 40));
      java.awt.Rectangle oldSelectionRect = selectionRect;
      int x = StrictMath.min(getWidth(), StrictMath.max(e.getX(), 0));
      int y = StrictMath.min(getHeight(), StrictMath.max(e.getY(), 0));
      selectionRect =
          new java.awt.Rectangle
              (
                  StrictMath.min(startDragPoint.x, x),
                  StrictMath.min(startDragPoint.y, y),
                  StrictMath.abs(x - startDragPoint.x),
                  StrictMath.abs(y - startDragPoint.y)
              );
      selectWidgets(selectionRect);
      if (oldSelectionRect != null) {
        glassPane.repaint(oldSelectionRect);
      }
      glassPane.repaint(selectionRect);
    }
  }

  public void mouseEntered(java.awt.event.MouseEvent e) {
  }

  public void mouseExited(java.awt.event.MouseEvent e) {
  }

  public void mouseClicked(java.awt.event.MouseEvent e) {
  }

  public void mousePressed(java.awt.event.MouseEvent e) {
    if (e.isPopupTrigger()) {
      doPopup(e);
      return;
    }
    if (!org.nlogo.awt.Mouse.hasButton1(e)) {
      return;
    }

    // this is so the user can use action keys to control buttons
    // - ST 8/6/04,8/31/04
    this.requestFocus();

    java.awt.Point p = e.getPoint();
    java.awt.Rectangle rect = this.getBounds();

    p.x += rect.x;
    p.y += rect.y;

    if (!rect.contains(p)) {
      return;
    }
    unselectWidgets();
    startDragPoint = e.getPoint();
    if (widgetCreator == null) {
      return;
    }
    Widget widget = widgetCreator.getWidget();
    if (widget == null) {
      return;
    }
    addWidget(widget, e.getX(), e.getY(), true, false);
    revalidate();
  }

  // this is bordering on comical its so confusing.
  // this method runs for the hubnet client editor.
  // im not yet sure if it runs anywhere else.
  // that seems like bugs waiting to happen. JC - 12/20/10
  protected void doPopup(java.awt.event.MouseEvent e) {
    javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.button"), "BUTTON", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.slider"), "SLIDER", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.switch"), "SWITCH", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.chooser"), "CHOOSER", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.input"), "INPUT", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.monitor"), "MONITOR", e.getX(), e.getY()));
    WidgetCreationMenuItem plot = new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.plot"), "PLOT", e.getX(), e.getY());
    // if there are no plots in this model, then you can't have a plot in a hubnet client.
    if (workspace.plotManager().plots().size() == 0) {
      plot.setEnabled(false);
    }
    menu.add(plot);
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.note"), "NOTE", e.getX(), e.getY()));
    menu.show(this, e.getX(), e.getY());
  }

  protected class WidgetCreationMenuItem
      extends javax.swing.JMenuItem {
    WidgetCreationMenuItem(final String displayName, final String name, final int x, final int y) {
      super(displayName);
      addActionListener
          (new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              createWidget(name, x, y);
            }
          });
    }
  }

  public void createWidget(String name, int x, int y) {
    Widget widget = makeWidget(name, false);
    WidgetWrapper wrapper = addWidget(widget, x, y, true, false);
    revalidate();
    wrapper.selected(true);
    wrapper.foreground();
    wrapper.isNew(true);
    new org.nlogo.window.Events.EditWidgetEvent(null)
        .raise(WidgetPanel.this);
    newWidget.setCursor
        (java.awt.Cursor.getPredefinedCursor
            (java.awt.Cursor.DEFAULT_CURSOR));
    wrapper.isNew(false);
    newWidget = null;
  }

  // This is used both when loading a model and when the user is making
  // new widgets in the UI.  For most widget types, the same type string
  // is used in both places. - ST 3/17/04
  public Widget makeWidget(String type, boolean loading) {
    type = "DUMMY " + type.toUpperCase();
    Widget fromRegistry = org.nlogo.window.WidgetRegistry.apply(type);
    if (fromRegistry != null) {
      return fromRegistry;
    } else if (type.equals("DUMMY SLIDER")) {
      return new org.nlogo.window.DummySliderWidget();
    } else if (type.equals("DUMMY CHOOSER") || // current name
        type.equals("DUMMY CHOICE"))   // old name, used in old models
    {
      return new org.nlogo.window.DummyChooserWidget
          (new org.nlogo.nvm.DefaultParserServices(workspace.parser()));
    } else if (type.equals("DUMMY BUTTON")) {
      return new org.nlogo.window.DummyButtonWidget();
    } else if (type.equals("DUMMY PLOT")) {
      // note that plots on the HubNet client must have the name of a plot
      // on the server, thus, feed the dummy plot widget the names of
      // the current plots so the user can select one. We override
      // this method in InterfacePanel since regular plots are handled
      // differently ev 1/25/07
      String[] names = (String[])workspace.plotManager().getPlotNames().toArray(scala.reflect.ClassTag$.MODULE$.apply(String.class));
      if (names.length > 0) {
        return DummyPlotWidget.apply(names[0], workspace.plotManager());
      } else {
        return DummyPlotWidget.apply("plot 1", workspace.plotManager());
      }
    } else if (type.equals("DUMMY MONITOR")) {
      return new org.nlogo.window.DummyMonitorWidget();
    } else if (type.equals("DUMMY INPUT") ||  // in the GUI, it's "Input Box"
        type.equals("DUMMY INPUTBOX"))  // in saved models, it's "INPUTBOX"
    {
      java.awt.Font font = new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont(),
          java.awt.Font.PLAIN, 12);
      return new org.nlogo.window.DummyInputBoxWidget
          (new org.nlogo.window.CodeEditor
              (1, 20, font, false, null, new EditorColorizer(workspace), I18N.guiJ().fn()),
              new org.nlogo.window.CodeEditor
                  (5, 20, font, true, null, new EditorColorizer(workspace), I18N.guiJ().fn()),
              this, new org.nlogo.nvm.DefaultParserServices(workspace.parser()));
    } else if (type.equals("DUMMY OUTPUT"))  // currently in saved models only - ST 3/17/04
    {
      return new org.nlogo.window.OutputWidget();
    } else if (type.equals("DUMMY CC-WINDOW")) // definitely in saved models only
    {
      // in current NetLogo versions, the command center goes in
      // a JSplitPane instead of in the InterfacePanel, so we ignore
      // the entry in the model - ST 7/13/04, 3/14/06
      return null;
    } else if (type.equals("DUMMY GRAPHICS-WINDOW") || type.equals("DUMMY VIEW") || type.equals("VIEW")) {
      view = new org.nlogo.window.DummyViewWidget(workspace.world());
      return view;
    } else {
      throw new IllegalStateException
          ("unknown widget type: " + type);
    }
  }

  public void mouseReleased(java.awt.event.MouseEvent e) {
    if (e.isPopupTrigger()) {
      doPopup(e);
      return;
    }
    if (org.nlogo.awt.Mouse.hasButton1(e)) {
      java.awt.Point p = e.getPoint();
      java.awt.Rectangle rect = this.getBounds();

      p.x += rect.x;
      p.y += rect.y;

      selectionRect = null;
      glassPane.setVisible(false);

      if (newWidget != null) {
        newWidget.selected(true);
        newWidget.foreground();
        newWidget.isNew(true);
        new org.nlogo.window.Events.EditWidgetEvent(null).raise(this);
        newWidget.setCursor
            (java.awt.Cursor.getPredefinedCursor
                (java.awt.Cursor.DEFAULT_CURSOR));
        newWidget.isNew(false);
        newWidget = null;
      }
    }
  }

  void setForegroundWrapper() {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; comps.length != i; i++) {
      if (comps[i] instanceof WidgetWrapper &&
          ((WidgetWrapper) comps[i]).selected()) {
        ((WidgetWrapper) comps[i]).foreground();
        return;
      }
    }
  }

  protected void selectWidgets(java.awt.Rectangle rect) {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; comps.length != i; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        boolean selected = false;
        WidgetWrapper wrapper = (WidgetWrapper) comps[i];

        selected = wrapper.selected();
        java.awt.Rectangle wrapperRect = wrapper.getUnselectedBounds();
        if (!selected) {
          if (rect.intersects(wrapperRect)) {
            wrapper.selected(true);
          }

        } else {
          if (!rect.intersects(wrapperRect) && selected) {
            wrapper.selected(false);
          }
        }

      }
    }
    setForegroundWrapper();
  }

  protected void unselectWidgets() {
    List<WidgetWrapper> selectedWrappers = selectedWrappers();
    for (int i = 0; i < selectedWrappers.size(); i++) {
      WidgetWrapper wrapper = selectedWrappers.get(i);
      wrapper.selected(false);
    }
  }

  protected WidgetWrapper addWidget(Widget widget, int x, int y,
                                    boolean select, boolean loadingWidget) {
    java.awt.Dimension size = widget.getSize();
    WidgetWrapper wrapper = new WidgetWrapper(widget, this);
    wrapper.setVisible(false);
    // we need to add the wrapper before we can call wrapper.getPreferredSize(), because
    // that method looks at its parent and sees if it's an InterfacePanel
    // and zooms accordingly - ST 6/16/02
    add(wrapper, DEFAULT_LAYER);
    moveToFront(wrapper);

    if (select || !loadingWidget) {
      wrapper.setSize(wrapper.getPreferredSize());
    } else {
      wrapper.setSize(size);////wrapper.getPreferredSize());
    }

    if (workspace.snapOn() && !loadingWidget) {
      int gridX = (x / GRID_SNAP) * GRID_SNAP;
      int gridY = (y / GRID_SNAP) * GRID_SNAP;
      wrapper.setLocation(gridX, gridY);
    } else {
      wrapper.setLocation(x, y);
    }
    wrapper.validate();
    wrapper.setVisible(true);

    zoomer().zoomWidget(wrapper, true, loadingWidget, 1.0, zoomFactor());

    if (select) {
      newWidget = wrapper;
      newWidget.originalBounds = newWidget.getBounds();
      newWidget.setCursor
          (java.awt.Cursor.getPredefinedCursor
              (java.awt.Cursor.CROSSHAIR_CURSOR));
    }
    org.nlogo.log.Logger.logAddWidget(widget.classDisplayName(), widget.displayName());
    return wrapper;
  }

  public void editWidgetFinished(org.nlogo.api.Editable target, boolean canceled) {
    if (target instanceof java.awt.Component
        && ((java.awt.Component) target).getParent() instanceof WidgetWrapper) {
      ((WidgetWrapper) ((java.awt.Component) target).getParent()).selected(false);
    }
    if (canceled && newWidget != null) {
      removeWidget(newWidget);
      revalidate();
    }
    setForegroundWrapper();
    // this doesn't do anything on the Mac, presumably because the focus doesn't never
    // actually returns to us after the edit dialog closed because isFocusable() is
    // already false, but just in case it's needed on some VM... - ST 8/6/04
    loseFocusIfAppropriate();
  }

  public void deleteSelectedWidgets() {
    List<WidgetWrapper> hitList =
        new ArrayList<WidgetWrapper>();
    List<WidgetWrapper> selectedWrappers = selectedWrappers();
    for (WidgetWrapper w : selectedWrappers) {
      if (w.selected() && w.widget().deleteable()) {
        hitList.add(w);
      }
    }
    deleteWidgets(hitList);
  }

  void deleteWidget(WidgetWrapper target) {
    List<WidgetWrapper> hitList =
        new ArrayList<WidgetWrapper>();
    hitList.add(target);
    deleteWidgets(hitList);
  }

  protected void deleteWidgets(List<WidgetWrapper> hitList) {
    for (int i = 0; i < hitList.size(); i++) {
      WidgetWrapper w = hitList.get(i);
      removeWidget(w);
    }
    setForegroundWrapper();
    revalidate();
    repaint(); // you wouldn't think this'd be necessary, but without it
    // the widget didn't visually disappear - ST 6/23/03
    loseFocusIfAppropriate();
  }

  protected void removeWidget(WidgetWrapper wrapper) {
    if (wrapper.widget() == view) {
      view = null;
    }
    remove(wrapper);
    org.nlogo.log.Logger.logWidgetRemoved(wrapper.widget().classDisplayName(),
        wrapper.widget().displayName());
  }

  // if sliderEventOnReleaseOnly is true, a SliderWidget will only raise an InterfaceGlobalEvent
  // when the mouse is released from the SliderDragControl
  // --mag 9/25/02, ST 4/9/03
  protected boolean sliderEventOnReleaseOnly = false;

  public boolean sliderEventOnReleaseOnly() {
    return sliderEventOnReleaseOnly;
  }

  public void sliderEventOnReleaseOnly(boolean sliderEventOnReleaseOnly) {
    this.sliderEventOnReleaseOnly = sliderEventOnReleaseOnly;
  }

  public void handle(org.nlogo.window.Events.ZoomedEvent e) {
    super.handle(e);
    unselectWidgets();
    zoomer().zoomWidgets(zoomFactor());
    revalidate();
  }

  /// loading and saving

  public Widget loadWidget(scala.collection.Seq<String> strings, final String modelVersion) {
    Widget.LoadHelper helper =
        new Widget.LoadHelper() {
          public String version() {
            return modelVersion;
          }

          public String convert(String source, boolean reporter) {
            return workspace.autoConvert(source, true, reporter, modelVersion);
          }
        };
    String type = strings.apply(0);
    int x = Integer.parseInt(strings.apply(1));
    int y = Integer.parseInt(strings.apply(2));
    Widget newGuy = makeWidget(type, true);
    if (newGuy != null) {
      newGuy.load(strings, helper);
      enforceMinimumAndMaximumWidgetSizes(newGuy);
      addWidget(newGuy, x, y, false, true);
    }
    return newGuy;
  }

  public void handle(org.nlogo.window.Events.WidgetEditedEvent e) {
    new org.nlogo.window.Events.DirtyEvent().raise(this);
    zoomer().updateZoomInfo(e.widget());
  }

  public void handle(org.nlogo.window.Events.WidgetRemovedEvent e) {
    // since all plot widgets on the client are subordinate to
    // plot widgets on the server remove the plot widget
    // on the client when the plot in the server is removed
    // ev 1/18/07
    if (e.widget() instanceof org.nlogo.window.PlotWidget) {
      java.awt.Component[] comps = getComponents();
      for (int i = 0; i < comps.length; i++) {
        if (comps[i] instanceof WidgetWrapper) {
          WidgetWrapper wrapper = (WidgetWrapper) comps[i];
          Widget widget = wrapper.widget();
          if (widget instanceof DummyPlotWidget &&
              e.widget().displayName().equals(widget.displayName())) {
            removeWidget(wrapper);
          }
        }
      }
      repaint();
    }
  }

  public void handle(org.nlogo.window.Events.LoadBeginEvent e) {
    unselectWidgets();
    removeAllWidgets();
    zoomer().forgetAllZoomInfo();
  }

  @Override
  public void loadWidgets(scala.collection.Seq<String> lines, String version) {
    try {
      scala.collection.Seq<scala.collection.Seq<String>> v =
          org.nlogo.api.ModelReader.parseWidgets(lines);
      if (null != v) {
        setVisible(false);
        for (scala.collection.Iterator<scala.collection.Seq<String>> iter = v.iterator(); iter.hasNext();) {
          loadWidget(iter.next(), version);
        }
      }
    } finally {
      setVisible(true);
      revalidate();
    }
  }

  @Override
  public List<Widget> getWidgetsForSaving() {
    List<Widget> result = new ArrayList<Widget>();
    java.awt.Component[] comps = getComponents();
    // loop backwards so JLayeredPane gives us the components
    // in back-to-front order for saving - ST 9/29/03
    for (int i = comps.length - 1; i >= 0; i--) {
      if (comps[i] instanceof WidgetWrapper) {
        WidgetWrapper wrapper = (WidgetWrapper) comps[i];
        Widget widget = wrapper.widget();
        if (!result.contains(widget)) {
          result.add(widget);
        }
      }
    }
    return result;
  }

  @Override
  public void removeAllWidgets() {
    java.awt.Component[] comps = getComponents();
    setVisible(false);
    for (int i = 0; comps.length != i; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        WidgetWrapper wrapper = (WidgetWrapper) comps[i];
        removeWidget(wrapper);
      }
    }
  }

  private org.nlogo.window.Widget view = null;

  @Override
  public boolean hasView() {
    return (view != null);
  }

  boolean contains(org.nlogo.api.Editable w) {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; comps.length != i; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        WidgetWrapper wrapper = (WidgetWrapper) comps[i];
        if (wrapper.widget().getEditable() == w) {
          return true;
        }
      }
    }

    return false;
  }

  protected void enforceMinimumAndMaximumWidgetSizes(java.awt.Component component) {
    java.awt.Dimension size = component.getSize();
    boolean changed = false;

    // enforce minimum
    java.awt.Dimension minimumSize = component.getMinimumSize();
    if (size.width < minimumSize.width) {
      size.width = minimumSize.width;
      changed = true;
    }
    if (size.height < minimumSize.height) {
      size.height = minimumSize.height;
      changed = true;
    }

    // enforce maximum
    java.awt.Dimension maximumSize = component.getMaximumSize();
    if (maximumSize != null) {
      if (size.width > maximumSize.width && maximumSize.width > 0) {
        size.width = maximumSize.width;
        changed = true;
      }
      if (size.height > maximumSize.height && maximumSize.height > 0) {
        size.height = maximumSize.height;
        changed = true;
      }
    }

    if (changed) {
      component.setSize(size);
    }
  }

  /// buttons

  protected void loseFocusIfAppropriate() {
    if (hasFocus && !isFocusable()) {
      transferFocus();
    }
  }

  @Override
  public boolean isFocusable() {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        Widget widget = ((WidgetWrapper) comps[i]).widget();
        if (widget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) widget;
          if (button.actionKey() != '\0' &&
              button.actionKey() != ' ') {
            return true;
          }
        }
      }
    }
    return false;
  }

  /// dispatch WidgetContainer methods

  public String getBoundsString(Widget widget) {
    StringBuilder buf = new StringBuilder();
    java.awt.Rectangle r = getUnzoomedBounds(widget);
    buf.append(r.x + "\n");
    buf.append(r.y + "\n");
    buf.append((r.x + r.width) + "\n");
    buf.append((r.y + r.height) + "\n");
    return buf.toString();
  }

  public java.awt.Rectangle getUnzoomedBounds(java.awt.Component component) {
    return zoomer().getUnzoomedBounds(component);
  }

  public void resetZoomInfo(Widget widget) {
    zoomer().updateZoomInfo(widget);
  }

  public void resetSizeInfo(Widget widget) {
    getWrapper(widget).widgetResized();
  }

  public boolean isZoomed() {
    return zoomer().zoomFactor() != 1.0;
  }

  public boolean canAddWidget(String widget) {
    if (widget.equals(I18N.guiJ().get("tabs.run.widgets.view"))) {
      return !hasView();
    } else if (widget.equals(I18N.guiJ().get("tabs.run.widgets.plot"))) {
      // you can't add a plot to the client interface unless
      // there are plots in the server interface so enable the
      // plot button accordingly ev 1/25/07
      return workspace.plotManager().getPlotNames().nonEmpty();
    }

    return true;
  }


}
