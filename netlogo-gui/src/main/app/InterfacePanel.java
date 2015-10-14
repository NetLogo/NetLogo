// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import org.nlogo.api.I18N;
import org.nlogo.api.ModelSectionJ;
import org.nlogo.api.Version;
import org.nlogo.api.VersionHistory;
import org.nlogo.window.EditorColorizer;
import org.nlogo.window.Widget;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

strictfp class InterfacePanel
    extends WidgetPanel
    implements java.awt.event.KeyListener,
    org.nlogo.window.Events.LoadSectionEvent.Handler,
    org.nlogo.window.Events.ExportInterfaceEvent.Handler {

  private final org.nlogo.window.ViewWidgetInterface viewWidget;

  public org.nlogo.window.ViewWidgetInterface viewWidget() {
    return viewWidget;
  }

  public InterfacePanel(org.nlogo.window.ViewWidgetInterface viewWidget,
                        org.nlogo.window.GUIWorkspace workspace) {
    super(workspace);
    this.viewWidget = viewWidget;
    workspace.setWidgetContainer(this);
    // in 3d don't add the view widget since it's always
    // disabled there's no reason for it to take space 7/5/07
    if (!Version.is3D()) {
      addWidget((Widget) viewWidget, 0, 0, false, false);
    }
    ((Widget) viewWidget).deleteable_$eq(false);
    addKeyListener(this);
    addMouseListener(this);
  }

  ///

  @Override
  public void focusGained(java.awt.event.FocusEvent e) {
    hasFocus = true;
    enableButtonKeys(true);
  }

  @Override
  public void focusLost(java.awt.event.FocusEvent e) {
    hasFocus = false;
    enableButtonKeys(false);
  }

  ///

  @Override
  protected void doPopup(final java.awt.event.MouseEvent e) {
    JPopupMenu menu = new JPopupMenu();
    // add all the widgets
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.button"), "BUTTON", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.slider"), "SLIDER", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.switch"), "SWITCH", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.chooser"), "CHOOSER", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.input"), "INPUT", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.monitor"), "MONITOR", e.getX(), e.getY()));
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.plot"), "PLOT", e.getX(), e.getY()));
    WidgetCreationMenuItem outputItem =
        new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.output"), "OUTPUT", e.getX(), e.getY());
    if (getOutputWidget() != null) {
      outputItem.setEnabled(false);
    }
    menu.add(outputItem);
    menu.add(new WidgetCreationMenuItem(I18N.guiJ().get("tabs.run.widgets.note"), "NOTE", e.getX(), e.getY()));

    // add extra stuff
    menu.add(new javax.swing.JPopupMenu.Separator());
    menu.add(exportItem());

    menu.show(this, e.getX(), e.getY());
  }

  private JMenuItem exportItem() {
    JMenuItem exportItem = new javax.swing.JMenuItem("Export Interface...");
    exportItem.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            try {
              exportInterface();
            } catch (java.io.IOException ex) {
              javax.swing.JOptionPane.showMessageDialog
                  (InterfacePanel.this, ex.getMessage(),
                      I18N.guiJ().get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE);
            }
          }
        });
    return exportItem;
  }

  private class WidgetCreationMenuItem
      extends javax.swing.JMenuItem {
    WidgetCreationMenuItem(final String displayName, final String widgetType, final int x, final int y) {
      super(displayName);
      addActionListener
          (new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              Widget widget = makeWidget(widgetType, false);
              WidgetWrapper wrapper = addWidget(widget, x, y, true, false);
              revalidate();
              wrapper.selected(true);
              wrapper.foreground();
              wrapper.isNew(true);
              new org.nlogo.window.Events.EditWidgetEvent(null)
                  .raise(InterfacePanel.this);
              newWidget.setCursor
                  (java.awt.Cursor.getPredefinedCursor
                      (java.awt.Cursor.DEFAULT_CURSOR));
              wrapper.isNew(false);
              newWidget = null;
            }
          });
    }
  }

  // This is used both when loading a model and when the user is making
  // new widgets in the UI.  For most widget types, the same type string
  // is used in both places. - ST 3/17/04
  @Override
  public Widget makeWidget(String type, boolean loading) {
    type = type.toUpperCase();
    Widget fromRegistry = org.nlogo.window.WidgetRegistry.apply(type);
    if (fromRegistry != null) {
      return fromRegistry;
    } else if (type.equalsIgnoreCase("SLIDER")) {
      return new org.nlogo.window.SliderWidget(workspace.world.auxRNG) {
        @Override
        public int sourceOffset() {
          return org.nlogo.workspace.Evaluator.sourceOffset(org.nlogo.agent.Observer.class, false);
        }
      };
    } else if (type.equals("CHOOSER") || // current name
        type.equals("CHOICE"))   // old name, used in old models
    {
      return new org.nlogo.window.ChooserWidget(workspace);
    } else if (type.equals("BUTTON")) {
      return new org.nlogo.window.ButtonWidget(workspace.world.mainRNG);
    } else if (type.equals("PLOT")) {
      return org.nlogo.window.PlotWidget.apply(workspace.plotManager());
    } else if (type.equals("MONITOR")) {
      return new org.nlogo.window.MonitorWidget(workspace.world.auxRNG);
    } else if (type.equals("INPUT") ||  // in the GUI, it's "Input Box"
        type.equals("INPUTBOX"))  // in saved models, it's "INPUTBOX"
    {
      java.awt.Font font = new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont(),
          java.awt.Font.PLAIN, 12);
      return new org.nlogo.window.InputBoxWidget
          (new org.nlogo.window.CodeEditor
              (1, 20, font, false, null, new EditorColorizer(workspace), I18N.guiJ().fn()),
              new org.nlogo.window.CodeEditor
                  (5, 20, font, true, null, new EditorColorizer(workspace), I18N.guiJ().fn()),
              workspace, this);
    } else if (type.equals("OUTPUT"))  // currently in saved models only - ST 3/17/04
    {
      return new org.nlogo.window.OutputWidget();
    } else if (type.equals("CC-WINDOW")) // definitely in saved models only
    {
      // in current NetLogo versions, the command center goes in
      // a JSplitPane instead of in the InterfacePanel, so we ignore
      // the entry in the model - ST 7/13/04, 3/14/06
      return null;
    } else {
      throw new IllegalStateException
          ("unknown widget type: " + type);
    }
  }

  @Override
  protected void deleteWidgets(List<WidgetWrapper> hitList) {
    boolean needsRecompile = false;
    for (int i = 0; i < hitList.size(); i++) {
      WidgetWrapper w = hitList.get(i);
      removeWidget(w);
      if (w.widget() instanceof org.nlogo.window.JobWidget) {
        // this ensures that the right thing happens if we delete
        // a button or monitor that doesn't compile; we need to remove it
        // from the errors tab - ST 12/17/04
        org.nlogo.window.JobWidget jobWidget =
            (org.nlogo.window.JobWidget) w.widget();
        jobWidget.innerSource("");
        new org.nlogo.window.Events.CompileMoreSourceEvent(jobWidget)
            .raise(this);
      }
      if (w.widget() instanceof org.nlogo.window.InterfaceGlobalWidget) {
        needsRecompile = true;
      }
    }
    setForegroundWrapper();
    revalidate();
    repaint(); // you wouldn't think this'd be necessary, but without it
    // the widget didn't visually disappear - ST 6/23/03
    if (needsRecompile) {
      new org.nlogo.window.Events.CompileAllEvent().raise(this);
    }
    loseFocusIfAppropriate();
  }

  @Override
  protected void removeWidget(WidgetWrapper wrapper) {
    remove(wrapper);

    // if the compile that is associated with this removal (assuming there is one) fails
    // the observer variables and constraints might not get reallocated in which case
    // if we try to add a different widget with the same name we get a constraint violation
    // from the old constraint. yuck.  ev 11/27/07
    new org.nlogo.window.Events.RemoveConstraintEvent
        (wrapper.widget().displayName())
        .raise(this);

    org.nlogo.log.Logger.logWidgetRemoved(wrapper.widget().classDisplayName(),
        wrapper.widget().displayName());
  }

  /// loading and saving

  @Override
  public Widget loadWidget(String[] strings, final String modelVersion) {
    return loadWidget(strings, modelVersion, 0, 0);
  }

  // TODO: consider cleaning up this x and y business
  // it was added for copying/pasting widgets.
  // the regular loadWidget just uses the x and y from the string array
  // it passes in x=0, y=0 and we do a check. ugly, but works for now.
  // paste uses the x and y from the right click location.
  private Widget loadWidget(String[] strings, final String modelVersion, int x, int y) {
    Widget.LoadHelper helper =
        new Widget.LoadHelper() {
          public String version() {
            return modelVersion;
          }

          public String convert(String source, boolean reporter) {
            return workspace.autoConvert
                (source, true, reporter, modelVersion);
          }
        };
    String type = strings[0];
    if (x == 0) {
      x = Integer.parseInt(strings[1]);
    }
    if (y == 0) {
      y = Integer.parseInt(strings[2]);
    }
    if (viewWidget instanceof org.nlogo.window.ViewWidget &&
        !type.equals("GRAPHICS-WINDOW") &&
        VersionHistory.olderThan13pre1(modelVersion)) {
      y += ((org.nlogo.window.ViewWidget) viewWidget).getExtraHeight() +
          ((org.nlogo.window.ViewWidget) viewWidget).controlStrip.getHeight();
    }
    if (type.equals("GRAPHICS-WINDOW")) {
      // the graphics widget (and the command center) are special cases because
      // they are not recreated at load time, but reused
      viewWidget.asWidget().load(strings, helper);
      // in 3D we don't add the viewWidget to the interface panel
      // so don't worry about all the sizing junk ev 7/5/07
      java.awt.Container parent = viewWidget.asWidget().getParent();
      if (parent != null) {
        parent.setSize(viewWidget.asWidget().getSize());
        enforceMinimumAndMaximumWidgetSizes(viewWidget.asWidget());
        parent.setLocation(x, y);
        zoomer().zoomWidgetLocation
            (getWrapper(viewWidget.asWidget()),
                true, true, 1.0, zoomer().zoomFactor());
        zoomer().zoomWidgetSize
            (getWrapper(viewWidget.asWidget()),
                true, true, 1.0, zoomer().zoomFactor());
        zoomer().scaleComponentFont
            (((org.nlogo.window.ViewWidget) viewWidget)
                .view,
               zoomFactor(), 1.0, false);
      }
      return viewWidget.asWidget();
    } else {
      Widget newGuy = null;
      newGuy = makeWidget(type, true);
      if (newGuy != null) {
        newGuy.load(strings, helper);
        enforceMinimumAndMaximumWidgetSizes(newGuy);
        addWidget(newGuy, x, y, false, true);
      }
      return newGuy;
    }
  }

  @Override
  public List<org.nlogo.window.Widget> getWidgetsForSaving() {
    List<org.nlogo.window.Widget> result =
        new ArrayList<org.nlogo.window.Widget>();
    java.awt.Component[] comps = getComponents();
    // automatically add the view widget in 3D isn't not
    // in the comp list but we definitely want to save it
    // it won't be added twice as we're checking contains
    // below.  ev 7/5/07
    result.add((org.nlogo.window.Widget) viewWidget);
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
  boolean contains(org.nlogo.api.Editable w) {
    if (w == ((Widget) viewWidget).getEditable()) {
      return true;
    }
    return super.contains(w);
  }

  @Override
  public void handle(org.nlogo.window.Events.WidgetRemovedEvent e) {
  }

  public void handle(org.nlogo.window.Events.ExportInterfaceEvent e) {
    try {
      javax.imageio.ImageIO.write
          (org.nlogo.awt.Images.paintToImage(this),
              "png", e.stream);
    } catch (java.io.IOException ex) {
      e.exceptionBox[0] = ex;
    }
  }

  private void exportInterface()
      throws java.io.IOException {
    try {
      final String exportPath =
          org.nlogo.swing.FileDialog.show
              (this, "Export Interface",
                  java.awt.FileDialog.SAVE,
                  workspace.guessExportName("interface.png"));
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(this),
        "Exporting...",
        new Runnable() {
          public void run() {
            try {
              workspace.exportInterface(exportPath);
            } catch (java.io.IOException ex) {
              exception[0] = ex;
            }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    } catch (org.nlogo.awt.UserCancelException ex) {
      org.nlogo.util.Exceptions.ignore(ex);
    }
  }

  public void handle(org.nlogo.window.Events.LoadSectionEvent e) {
    if (e.section == ModelSectionJ.WIDGETS()) {
      loadWidgets(e.lines, e.version);
    }
  }

  @Override
  public void removeAllWidgets() {
    try {
      java.awt.Component[] comps = getComponents();
      setVisible(false);
      for (int i = 0; comps.length != i; i++) {
        if (comps[i] instanceof WidgetWrapper) {
          WidgetWrapper wrapper = (WidgetWrapper) comps[i];
          if (wrapper.widget() != viewWidget) {
            removeWidget(wrapper);
          }
        }
      }
    } catch (RuntimeException ex) {
      org.nlogo.util.Exceptions.handle(ex);
    } finally {
      setVisible(false);
    }
  }

  /// buttons

  @Override
  public boolean isFocusable() {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        Widget widget = ((WidgetWrapper) comps[i]).widget();
        if (widget instanceof org.nlogo.window.InputBoxWidget) {
          return true;
        }
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

  private org.nlogo.window.ButtonWidget findActionButton(char key) {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        Widget widget = ((WidgetWrapper) comps[i]).widget();
        if (widget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) widget;
          if (Character.toUpperCase(button.actionKey()) ==
              Character.toUpperCase(key)) {
            return button;
          }
        }
      }
    }
    return null;
  }

  private void enableButtonKeys(boolean enabled) {
    java.awt.Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof WidgetWrapper) {
        Widget widget = ((WidgetWrapper) comps[i]).widget();
        if (widget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) widget;
          button.keyEnabled(enabled);
        }
      }
    }
  }

  public void keyTyped(java.awt.event.KeyEvent e) {
    if (e.getKeyChar() != java.awt.event.KeyEvent.CHAR_UNDEFINED &&
        !e.isActionKey() &&
        (e.getModifiers() & getToolkit().getMenuShortcutKeyMask()) == 0) {
      org.nlogo.window.ButtonWidget button =
          findActionButton(e.getKeyChar());
      if (button != null) {
        button.keyTriggered();
      }
    }
  }

  public void keyPressed(KeyEvent evt) {
  }

  public void keyReleased(KeyEvent evt) {
  }

  @Override
  public boolean canAddWidget(String widget) {
    return (!widget.equals("Output")) || (getOutputWidget() == null);
  }
}
