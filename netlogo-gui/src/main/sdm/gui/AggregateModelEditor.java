// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.framework.DrawingEditor;
import org.jhotdraw.framework.DrawingView;
import org.jhotdraw.framework.Figure;
import org.jhotdraw.framework.FigureEnumeration;
import org.jhotdraw.framework.Tool;
import org.jhotdraw.framework.ViewChangeListener;
import org.jhotdraw.util.UndoManager;
import org.nlogo.core.CompilerException;
import org.nlogo.api.CompilerServices;
import org.nlogo.core.I18N;
import org.nlogo.core.TokenType;
import org.nlogo.sdm.Model;
import org.nlogo.window.EditDialogFactoryInterface;

import javax.swing.JMenuBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

strictfp class AggregateModelEditor
    extends javax.swing.JFrame
    implements
    DrawingEditor,
    org.nlogo.window.Event.LinkChild {

  /// Constants

  private static final Dimension WINDOW_SIZE = new Dimension(700, 550);
  private static final Dimension VIEW_SIZE = new Dimension(800, 1000);

  /// Instance variables

  private final org.nlogo.editor.Colorizer colorizer;
  private final java.awt.Component linkParent;
  private final org.nlogo.window.MenuBarFactory menuBarFactory;
  private final CompilerServices compiler;
  private final AggregateDrawing drawing;
  private AggregateModelEditorToolBar toolbar;
  private final UndoManager undoManager;
  private Tool currentTool;
  private Tool selectionTool;
  private DrawingView view;
  private AggregateTabs tabs;
  private org.nlogo.window.ErrorLabel errorLabel;
  private final EditDialogFactoryInterface dialogFactory;

  AggregateModelEditor
      (java.awt.Component linkParent,
       org.nlogo.editor.Colorizer colorizer,
       org.nlogo.window.MenuBarFactory menuBarFactory,
       CompilerServices compiler,
       EditDialogFactoryInterface dialogFactory) {
    this(linkParent, colorizer, menuBarFactory, null, compiler, dialogFactory);
  }

  AggregateModelEditor
      (java.awt.Component linkParent,
       org.nlogo.editor.Colorizer colorizer,
       org.nlogo.window.MenuBarFactory menuBarFactory,
       AggregateDrawing drawing,
       CompilerServices compiler,
       EditDialogFactoryInterface dialogFactory) {
    super(I18N.guiJ().get("menu.tools.systemDynamicsModeler"), linkParent.getGraphicsConfiguration());
    undoManager = new UndoManager();

    this.linkParent = linkParent;
    this.colorizer = colorizer;
    this.menuBarFactory = menuBarFactory;
    this.compiler = compiler;
    this.dialogFactory = dialogFactory;

    Wrapper.reset();

    // You might think it'd make more sense to dispose of the window --
    // not hide it -- when it's closed. But disposing and then recreating
    // the window causes the OS X screen menu bar to go nuts, for
    // reasons I don't pretend to understand. So, the editor gets made
    // at most once per model. After it's been made, it hides and shows
    // but is not disposed of until the model is closed. - AZS 6/18/05
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    this.drawing = (drawing == null) ? new AggregateDrawing() : drawing;

    DrawingView drawingView =
        new AggregateDrawingView
            (this, VIEW_SIZE.width, VIEW_SIZE.height);
    drawingView.setDrawing(this.drawing);


    open(drawingView);

    pack();
    setVisible(true);
  }

  void setError(org.nlogo.api.SourceOwner owner, CompilerException e) {
    errorLabel.setError(e, owner.headerSource().length());
    tabs.setError(e);
  }

  public Object getLinkParent() {
    return linkParent;
  }

  void setToolbar(AggregateModelEditorToolBar toolbar) {
    this.toolbar = toolbar;
  }

  void inspectFigure(org.jhotdraw.framework.Figure f) {
    try {
      org.nlogo.api.Editable target =
          (org.nlogo.api.Editable) f;

      // makes a dialog and returns a boolean result. we ignore the result - ST 3/2/09
      dialogFactory.canceled(this, target);

      if (f instanceof ModelElementFigure && ((ModelElementFigure) f).dirty()) {
        new org.nlogo.window.Events.CompileAllEvent().raise(this);
        new org.nlogo.window.Events.DirtyEvent().raise(this);
      }

      f.invalidate();
    } catch (ClassCastException ex) {
      // if it's not editable, do nothing
      org.nlogo.api.Exceptions.ignore(ex);
    }
  }

  public AggregateDrawing getDrawing() {
    return drawing;
  }

  /**
   * Translates the model into NetLogo code.
   */
  public String toNetLogoCode() {
    String src = "";

    if (drawing != null) {
      org.nlogo.sdm.Translator translator =
          new org.nlogo.sdm.Translator(drawing.getModel(), compiler);
      src = translator.source();
    }

    return src;
  }

  public void setTool(Tool t) {
    currentTool = t;
    if (tool() != null) {
      tool().activate();
    }
  }

  public void setSelectionTool() {
    if (selectionTool != null) {
      setTool(selectionTool);
    }
  }

  private void open(final DrawingView view) {
    getContentPane().setLayout(new java.awt.BorderLayout());
    this.view = view;

    errorLabel = new org.nlogo.window.ErrorLabel();
    getContentPane().add(errorLabel, BorderLayout.NORTH);

    AggregateEditorTab editorTab =
        new AggregateEditorTab(this, new AggregateModelEditorToolBar(this, drawing.getModel()), (Component) view);
    tabs =
        new AggregateTabs(editorTab, this, colorizer);
    getContentPane().add(tabs, BorderLayout.CENTER);


    // Build the menu bar. For OS X, we add a bunch of the menus from app
    // so that the screen menu bar looks consistent. - AZS 6/17/05
    JMenuBar menuBar = new javax.swing.JMenuBar();

    boolean isOSX = System.getProperty("os.name").startsWith("Mac");

    if (isOSX) {
      menuBar.add(menuBarFactory.createFileMenu());
    }

    org.jhotdraw.util.CommandMenu editMenu =
        new org.jhotdraw.util.CommandMenu(I18N.guiJ().get("menu.edit"));
    editMenu.add(new org.jhotdraw.util.UndoCommand(I18N.guiJ().get("menu.edit.undo"), this));
    editMenu.add(new org.jhotdraw.util.RedoCommand(I18N.guiJ().get("menu.edit.redo"), this));
    menuBar.add(editMenu);

    if (isOSX) {
      menuBar.add(menuBarFactory.createToolsMenu());
      javax.swing.JMenuItem zoomMenu =
          menuBar.add(menuBarFactory.createZoomMenu());
      zoomMenu.setEnabled(false);
      menuBar.add(zoomMenu);
    }

    menuBar.add(new org.nlogo.swing.TabsMenu(I18N.guiJ().get("menu.tabs"), tabs));

    if (isOSX) {
      menuBarFactory.addHelpMenu(menuBar);
    }

    setJMenuBar(menuBar);

    setSize(WINDOW_SIZE);
    setVisible(true);

    selectionTool = new InspectionTool(this);
    setTool(selectionTool);

    setVisible(true);

    org.nlogo.awt.EventQueue.invokeLater
        (new Runnable() {
          public void run() {
            toFront();
          }
        });
  }


  /// From interface DrawingEditor

  /**
   * Retrieve the active view from the window
   * Gets the current drawing view.
   *
   * @see DrawingEditor
   */
  public DrawingView view() {
    return view;
  }

  public DrawingView[] views() {
    return new DrawingView[]{view()};
  }

  /**
   * Gets the current tool.
   *
   * @see DrawingEditor
   */
  public Tool tool() {
    return currentTool;
  }

  public void toolDone() {
    setSelectionTool();
    toolbar.popButtons();
  }

  public UndoManager getUndoManager() {
    return undoManager;
  }

  /**
   * Empty implementation.
   *
   * @see DrawingEditor
   */
  public void figureSelectionChanged(DrawingView view) {
  }

  /**
   * Empty implementation.
   *
   * @see DrawingEditor
   */
  public void showStatus(String str) {
  }

  /**
   * Register to hear when the active view is changed.  For Single document
   * interface, this will happen when a new drawing is created.
   *
   * @see DrawingEditor
   */
  public void addViewChangeListener(ViewChangeListener vcl) {
  }

  /**
   * Remove listener
   *
   * @see DrawingEditor
   */
  public void removeViewChangeListener(ViewChangeListener vcl) {
  }


}
