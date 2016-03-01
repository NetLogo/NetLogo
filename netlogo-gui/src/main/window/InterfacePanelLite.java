// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.CompilerServices;
import org.nlogo.api.ModelReader;
import org.nlogo.api.ModelSectionJ;
import org.nlogo.api.RandomServices;
import org.nlogo.api.Version;
import org.nlogo.api.VersionHistory;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public strictfp class InterfacePanelLite
    extends javax.swing.JLayeredPane
    implements
    WidgetContainer,
    java.awt.event.FocusListener,
    org.nlogo.window.Events.LoadSectionEvent.Handler,
    org.nlogo.window.Events.OutputEvent.Handler {

  private final Map<String, Widget> widgets; // widget name -> Widget
  private final ViewWidgetInterface viewWidget;

  public ViewWidgetInterface viewWidget() {
    return viewWidget;
  }

  private final CompilerServices compiler;
  private final RandomServices random;
  private final org.nlogo.plot.PlotManager plotManager;
  private final EditorFactory editorFactory;

  public InterfacePanelLite(ViewWidgetInterface viewWidget,
                            CompilerServices compiler,
                            RandomServices random,
                            org.nlogo.plot.PlotManager plotManager,
                            EditorFactory editorFactory) {
    this.viewWidget = viewWidget;
    this.compiler = compiler;
    this.random = random;
    this.plotManager = plotManager;
    this.editorFactory = editorFactory;
    widgets = new HashMap<String, Widget>();
    setOpaque(true);
    setBackground(java.awt.Color.WHITE);
    addFocusListener(this);
    addMouseListener
        (new java.awt.event.MouseAdapter() {
          @Override
          public void mousePressed(java.awt.event.MouseEvent e) {
            if (e.isPopupTrigger()) {
              doPopup(e);
            } else {
              // this is so the user can use action keys to control buttons
              // - ST 8/31/04
              requestFocus();
            }
          }

          @Override
          public void mouseReleased(java.awt.event.MouseEvent e) {
            if (e.isPopupTrigger()) {
              doPopup(e);
            }
          }
        });
    addKeyListener(getKeyAdapter());
    addWidget((Widget) viewWidget, 0, 0);
  }

  // made protected so that hubnet could override it to implement message throttling. -JC 8/19/10
  protected java.awt.event.KeyAdapter getKeyAdapter() {
    return new ButtonKeyAdapter();
  }

  // This is accessible ONLY FOR hubnet. Use it at your own peril. -- RG 6/11/15
  public class ButtonKeyAdapter extends java.awt.event.KeyAdapter {
    protected boolean keyIsHandleable(KeyEvent e) {
      return e.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
          !e.isActionKey() &&
          (e.getModifiers() & getToolkit().getMenuShortcutKeyMask()) == 0;
    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {
      if (keyIsHandleable(e)) {
        ButtonWidget button = findActionButton(e.getKeyChar());
        if (button != null) {
          buttonKeyed(button);
        }
      }
    }

    public void buttonKeyed(ButtonWidget button) {
      button.keyTriggered();
    }
  }


  boolean hasFocus = true;

  public void focusGained(java.awt.event.FocusEvent e) {
    //System.out.println( "iP focus gained from " + e.getOppositeComponent() ) ;
    hasFocus = true;
    enableButtonKeys(true);
  }

  @Override
  public void requestFocus() {
    requestFocusInWindow();
  }


  public void reset() {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof PlotWidget) {
        plotManager.forgetPlot(((PlotWidget) comps[i]).plot());
      }
      if (!(comps[i] instanceof ViewWidgetInterface)) {
        remove(comps[i]);
      }
    }
  }

  private void enableButtonKeys(boolean enabled) {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof ButtonWidget) {
        ButtonWidget button =
            (ButtonWidget) comps[i];
        button.keyEnabled(enabled);
      }

    }
  }

  public void focusLost(java.awt.event.FocusEvent e) {
    //System.out.println( "iP focus lost to " + e.getOppositeComponent() ) ;
    hasFocus = false;
    enableButtonKeys(false);
  }


  protected ButtonWidget findActionButton(char key) {
    //System.out.println( "findActionButton" ) ;
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof ButtonWidget) {
        ButtonWidget button =
            (ButtonWidget) comps[i];
        if (Character.toUpperCase(button.actionKey()) ==
            Character.toUpperCase(key)) {
          return button;
        }
      }
    }
    return null;
  }

  @Override
  public boolean isOptimizedDrawingEnabled() {
    return false; // our children may overlap
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(0, 0);
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    int maxX = 0, maxY = 0;
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (!(comps[i] instanceof Widget)) {
        continue;
      }
      java.awt.Point location = comps[i].getLocation();
      java.awt.Dimension size = comps[i].getSize();
      int x = location.x + size.width;
      int y = location.y + size.height;
      if (x > maxX) {
        maxX = x;
      }
      if (y > maxY) {
        maxY = y;
      }
    }
    return new java.awt.Dimension(maxX, maxY);
  }

  private OutputWidget getOutputWidget() {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof OutputWidget) {
        return (OutputWidget) comps[i];
      }
    }
    return null;
  }

  /// output

  public void handle(org.nlogo.window.Events.OutputEvent e) {
    if (getOutputWidget() != null && !e.toCommandCenter) {
      if (e.clear) {
        getOutputWidget().outputArea().clear();
      }
      if (e.outputObject != null) {
        getOutputWidget().outputArea().append
            (e.outputObject, e.wrapLines);
      }
    }
  }

  ///

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
    return component.getBounds();
  }

  public void resetZoomInfo(Widget widget) {
  }

  public void resetSizeInfo(Widget widget) {
  }

  public boolean isZoomed() {
    return false;
  }

  ///

  private void doPopup(java.awt.event.MouseEvent e) {
    javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
    javax.swing.JMenuItem item;
    item = new javax.swing.JMenuItem(Version.version());
    item.setEnabled(false);
    menu.add(item);
    item = new javax.swing.JMenuItem(org.nlogo.util.SysInfo.getOSInfoString());
    item.setEnabled(false);
    menu.add(item);
    item = new javax.swing.JMenuItem(org.nlogo.util.SysInfo.getVMInfoString());
    item.setEnabled(false);
    menu.add(item);
    item = new javax.swing.JMenuItem(org.nlogo.util.SysInfo.getMemoryInfoString());
    item.setEnabled(false);
    menu.add(item);
    menu.show(this, e.getX(), e.getY());
  }

  ///

  private void addWidget(Widget widget, int x, int y) {
    // this is really no good in the long term, because widgets
    // don't have unique names. For now, who cares? - mmh
    widgets.put(widget.displayName(), widget);
    widget.addPopupListeners();
    add(widget, DEFAULT_LAYER);
    moveToFront(widget);
    widget.setLocation(x, y);
    widget.validate();
  }

  public void hideWidget(String widgetName) {
    Widget widget = widgets.get(widgetName);
    if (widget != null) {
      widget.setVisible(false);
    }
  }

  public void showWidget(String widgetName) {
    Widget widget = widgets.get(widgetName);
    if (widget != null) {
      widget.setVisible(true);
    }
  }

  ///

  // if sliderEventOnReleaseOnly is true, a SliderWidget will only raise an InterfaceGlobalEvent
  // when the mouse is released from the SliderDragControl
  // --mag 9/25/02, ST 4/9/03
  private boolean sliderEventOnReleaseOnly = false;

  public boolean sliderEventOnReleaseOnly() {
    return sliderEventOnReleaseOnly;
  }

  public void sliderEventOnReleaseOnly(boolean sliderEventOnReleaseOnly) {
    this.sliderEventOnReleaseOnly = sliderEventOnReleaseOnly;
  }

  /// loading and saving

  public Widget loadWidget(String[] strings, final String modelVersion) {
    Widget.LoadHelper helper =
        new Widget.LoadHelper() {
          public String version() {
            return modelVersion;
          }

          public String convert(String source, boolean reporter) {
            return compiler.autoConvert(source, true, reporter, modelVersion);
          }
        };
    try {
      String type = strings[0];
      int x = Integer.parseInt(strings[1]);
      int y = Integer.parseInt(strings[2]);
      if (!type.equals("GRAPHICS-WINDOW") &&
          VersionHistory.olderThan13pre1(modelVersion)) {
        y += viewWidget.getAdditionalHeight();
      }
      if (type.equals("GRAPHICS-WINDOW") || type.equals("VIEW")) {
        // the graphics widget (and the command center) are special cases because
        // they are not recreated at load time, but reused
        try {
          viewWidget.asWidget().load(strings, helper);
        } catch (RuntimeException ex) {
          org.nlogo.util.Exceptions.handle(ex);
        }
        viewWidget.asWidget().setSize
            (viewWidget.asWidget().getSize());
        viewWidget.asWidget().setLocation(x, y);
        return viewWidget.asWidget();
      } else {
        Widget newGuy = WidgetRegistry.apply(type);
        try {
          if (type.equals("MONITOR")) {
            newGuy = new MonitorWidget(random.auxRNG());
          } else if (type.equals("PLOT")) {
            newGuy = PlotWidget.apply(plotManager);
          } else if (type.equals("SLIDER")) {
            newGuy = new SliderWidget(sliderEventOnReleaseOnly, random.auxRNG());
          } else if (type.equals("CHOOSER") || // new models use this
              type.equals("CHOICE"))   // old models use this
          {
            newGuy = new ChooserWidget(compiler);
          } else if (type.equals("INPUTBOX")) {
            newGuy = new InputBoxWidget(editorFactory.newEditor(1, 20, false),
                editorFactory.newEditor(5, 20, true),
                compiler, this);
          } else if (type.equals("BUTTON")) {
            newGuy = new ButtonWidget(random.mainRNG());
          } else if (type.equals("OUTPUT")) {
            newGuy = new OutputWidget();
          }
        } catch (RuntimeException ex) {
          org.nlogo.util.Exceptions.handle(ex);
        }
        if (newGuy != null) {
          newGuy.load(strings, helper);
          addWidget(newGuy, x, y);
        }
        return newGuy;
      }
    } catch (RuntimeException ex) {
      org.nlogo.util.Exceptions.handle(ex);
      return null;
    }
  }

  public void handle(org.nlogo.window.Events.LoadSectionEvent e) {
    if (e.section == ModelSectionJ.WIDGETS()) {
      try {
        List<List<String>> v =
            ModelReader.parseWidgets(e.lines);
        if (null != v) {
          setVisible(false);
          for (List<String> v2 : v) {
            String[] strings = v2.toArray(new String[v2.size()]);
            loadWidget(strings, e.version);
          }
        }
      } finally {
        setVisible(true);
        revalidate();
      }
    }
  }

}
