// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import javax.swing.{ JFrame, JMenuBar, JMenuItem, WindowConstants }, WindowConstants.HIDE_ON_CLOSE
import java.awt.{ BorderLayout, Component, Dimension }

import org.jhotdraw.framework.{ DrawingEditor, DrawingView, Figure, FigureEnumeration, Tool, ViewChangeListener }

import org.jhotdraw.util.{ CommandMenu, RedoCommand, UndoCommand, UndoManager }

import org.nlogo.core.{ CompilerException, I18N, TokenType }
import org.nlogo.api.{ CompilerServices, Editable, SourceOwner }
import org.nlogo.editor.Colorizer
import org.nlogo.sdm.{ Model, Translator }
import org.nlogo.window.{ EditDialogFactoryInterface, MenuBarFactory }
import org.nlogo.window.Event.LinkChild

object AggregateModelEditor {
  /// Constants
  val WindowSize: Dimension = new Dimension(700, 550)
  private val ViewSize: Dimension = new Dimension(800, 1000)
}

import AggregateModelEditor._

class AggregateModelEditor(
  linkParent: Component,
  colorizer: Colorizer,
  menuBarFactory: MenuBarFactory,
  val drawing: AggregateDrawing,
  compiler: CompilerServices,
  dialogFactory: EditDialogFactoryInterface) extends JFrame(
    I18N.gui.get("menu.tools.systemDynamicsModeler"), linkParent.getGraphicsConfiguration)
  with DrawingEditor
  with LinkChild {

  def this(
    linkParent: Component,
    colorizer: Colorizer,
    menuBarFactory: MenuBarFactory,
    compiler: CompilerServices,
    dialogFactory: EditDialogFactoryInterface) =
      this(linkParent, colorizer, menuBarFactory, new AggregateDrawing(), compiler, dialogFactory)

  private val undoManager: UndoManager = new UndoManager()
  private var currentTool: Option[Tool] = None

  locally {
    Wrapper.reset()

    // You might think it'd make more sense to dispose of the window --
    // not hide it -- when it's closed. But disposing and then recreating
    // the window causes the OS X screen menu bar to go nuts, for
    // reasons I don't pretend to understand. So, the editor gets made
    // at most once per model. After it's been made, it hides and shows
    // but is not disposed of until the model is closed. - AZS 6/18/05
    setDefaultCloseOperation(HIDE_ON_CLOSE)
  }

  val view: DrawingView = new AggregateDrawingView(this, ViewSize.width, ViewSize.height)
  view.setDrawing(drawing)

  private val toolbar: AggregateModelEditorToolBar = new AggregateModelEditorToolBar(this, drawing.getModel)

  val tabs: AggregateTabs = {
    val editorTab = new AggregateEditorTab(toolbar, view.asInstanceOf[Component])
    val proceduresTab = new AggregateProceduresTab(colorizer)
    new AggregateTabs(this, editorTab, proceduresTab)
  }

  getContentPane.add(tabs)

  private val selectionTool = new InspectionTool(this)

  locally {
    // Build the menu bar. For OS X, we add a bunch of the menus from app
    // so that the screen menu bar looks consistent. - AZS 6/17/05
    val menuBar: JMenuBar = new JMenuBar()

    val isOSX = System.getProperty("os.name").startsWith("Mac");

    if (isOSX) {
      menuBar.add(menuBarFactory.createFileMenu());
    }

    val editMenu: CommandMenu = new CommandMenu(I18N.gui.get("menu.edit"))
    editMenu.add(new UndoCommand(I18N.gui.get("menu.edit.undo"), this))
    editMenu.add(new RedoCommand(I18N.gui.get("menu.edit.redo"), this))

    menuBar.add(editMenu)

    if (isOSX) {
      menuBar.add(menuBarFactory.createToolsMenu())
      val zoomMenu: JMenuItem =
        menuBar.add(menuBarFactory.createZoomMenu())
      zoomMenu.setEnabled(false)
      menuBar.add(zoomMenu)
    }

    menuBar.add(new org.nlogo.swing.TabsMenu(I18N.gui.get("menu.tabs"), tabs))

    if (isOSX) {
      menuBarFactory.addHelpMenu(menuBar)
    }

    setJMenuBar(menuBar)

    setTool(selectionTool)
    setPreferredSize(WindowSize)
    pack()
    setVisible(true)

    org.nlogo.awt.EventQueue.invokeLater(new Runnable() {
      def run(): Unit = { toFront() }
    })
  }

  def clearError(): Unit = {
    tabs.clearError()
  }

  def setError(owner: SourceOwner, e: CompilerException): Unit = {
    if (e == null)
      tabs.clearError()
    else
      tabs.setError(e, owner.headerSource.length)
  }

  def getLinkParent: Component = linkParent

  def inspectFigure(f: Figure): Unit = {
    f match {
      case target: Editable =>
        // makes a dialog and returns a boolean result. we ignore the result - ST 3/2/09
        dialogFactory.canceled(this, target)

        f match {
          case mef: ModelElementFigure if mef.dirty =>
            new org.nlogo.window.Events.CompileAllEvent().raise(this)
            new org.nlogo.window.Events.DirtyEvent().raise(this)
          case _ =>
        }

        f.invalidate()
      case _ => // if it's not editable, do nothing
    }
  }

  /**
   * Translates the model into NetLogo code.
   */
  def toNetLogoCode: String = {
    new Translator(drawing.getModel, compiler).source
  }

  def setTool(t: Tool): Unit = {
    currentTool = Option(t)
    currentTool.foreach(_.activate())
  }

  def setSelectionTool(): Unit = {
    setTool(selectionTool)
  }


  /// From interface DrawingEditor
  def views: Array[DrawingView] = Array[DrawingView](view)

  /**
   * Gets the current tool.
   *
   * @see DrawingEditor
   */
  def tool: Tool = currentTool.orNull

  def toolDone(): Unit = {
    setSelectionTool()
    toolbar.popButtons()
  }

  def getUndoManager: UndoManager = undoManager

  /**
   * Empty implementation.
   *
   * @see DrawingEditor
   */
  def figureSelectionChanged(view: DrawingView): Unit = { }

  /**
   * Empty implementation.
   *
   * @see DrawingEditor
   */
  def showStatus(str: String): Unit = { }

  /**
   * Register to hear when the active view is changed.  For Single document
   * interface, this will happen when a new drawing is created.
   *
   * @see DrawingEditor
   */
  def addViewChangeListener(vcl: ViewChangeListener): Unit = { }

  /**
   * Remove listener
   *
   * @see DrawingEditor
   */
  def removeViewChangeListener(vcl: ViewChangeListener): Unit = { }
}
