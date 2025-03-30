// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ Component, Dimension, Graphics }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, JFrame, JMenuBar, JMenuItem, JPopupMenu, WindowConstants },
  WindowConstants.HIDE_ON_CLOSE
import javax.swing.border.LineBorder
import javax.swing.plaf.basic.BasicMenuUI

import org.jhotdraw.framework.{ DrawingEditor, DrawingView, Figure, Tool, ViewChangeListener }
import org.jhotdraw.util.{ Command, CommandMenu, RedoCommand, UndoCommand, UndoManager }

import org.nlogo.api.{ CompilerServices, SourceOwner }
import org.nlogo.awt.EventQueue
import org.nlogo.core.{ CompilerException, I18N, LiteralParser }
import org.nlogo.editor.Colorizer
import org.nlogo.sdm.Translator
import org.nlogo.swing.{ MenuItem, NetLogoIcon, Utils => SwingUtils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ Editable, EditDialogFactory, Events, MenuBarFactory }
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
  compiler: LiteralParser,
  dialogFactory: EditDialogFactory) extends JFrame(
    I18N.gui.get("menu.tools.systemDynamicsModeler"), linkParent.getGraphicsConfiguration)
  with DrawingEditor
  with LinkChild
  with Events.LoadBeginEvent.Handler
  with ThemeSync
  with NetLogoIcon {

  def this(
    linkParent: Component,
    colorizer: Colorizer,
    menuBarFactory: MenuBarFactory,
    compiler: CompilerServices,
    dialogFactory: EditDialogFactory) =
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

  val view = new AggregateDrawingView(this, ViewSize.width, ViewSize.height)

  view.setDrawing(drawing)

  private val toolbar: AggregateModelEditorToolBar = new AggregateModelEditorToolBar(this, drawing.getModel)

  val tabs: AggregateTabs = {
    val editorTab = new AggregateEditorTab(toolbar, view.asInstanceOf[Component])
    editorTab.setBorder(null)
    val proceduresTab = new AggregateProceduresTab(colorizer)
    new AggregateTabs(this, editorTab, proceduresTab)
  }

  getContentPane.add(tabs)

  private val selectionTool = new InspectionTool(this, drawing.getModel)

  private val menuBar: MenuBar = new MenuBar

  locally {
    // Build the menu bar. For OS X, we add a bunch of the menus from app
    // so that the screen menu bar looks consistent. - AZS 6/17/05

    val isOSX = System.getProperty("os.name").startsWith("Mac");

    if (isOSX) {
      menuBar.add(menuBarFactory.createFileMenu);
    }

    val editMenu = new SyncedCommandMenu(I18N.gui.get("menu.edit"))

    editMenu.add(new UndoCommand(I18N.gui.get("menu.edit.undo"), this))
    editMenu.add(new RedoCommand(I18N.gui.get("menu.edit.redo"), this))

    menuBar.add(editMenu)

    if (isOSX) {
      menuBar.add(menuBarFactory.createToolsMenu)
      val zoomMenu: JMenuItem =
        menuBar.add(menuBarFactory.createZoomMenu)
      zoomMenu.setEnabled(false)
      menuBar.add(zoomMenu)
    }

    menuBar.add(new TabsMenu(I18N.gui.get("menu.tabs"), tabs))

    if (isOSX) {
      val helpMenu = menuBarFactory.createHelpMenu
      menuBar.add(helpMenu)
      try {
        menuBar.setHelpMenu(helpMenu)
      } catch {
        case e: Error => org.nlogo.api.Exceptions.ignore(e)
      }
    }

    setJMenuBar(menuBar)

    setTool(selectionTool)
    setPreferredSize(WindowSize)
    pack()
    setVisible(true)

    EventQueue.invokeLater(new Runnable() {
      def run(): Unit = { toFront() }
    })
  }

  syncTheme()

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
        dialogFactory.canceled(this, target, false)

        f match {
          case mef: ModelElementFigure if mef.dirty =>
            new org.nlogo.window.Events.CompileAllEvent().raise(this)
            new org.nlogo.window.Events.DirtyEvent(None).raise(this)
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
    drawing.synchronizeModel()
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

  def handle(e: Events.LoadBeginEvent) {
    undoManager.clearUndos()
    undoManager.clearRedos()
  }

  override def syncTheme(): Unit = {
    menuBar.syncTheme()
    tabs.syncTheme()
    toolbar.syncTheme()
    view.syncTheme()
  }

  private class MenuBar extends JMenuBar with ThemeSync {
    override def paintComponent(g: Graphics) {
      val g2d = SwingUtils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.menuBackground())
      g2d.fillRect(0, 0, getWidth, getHeight)
    }

    override def paintBorder(g: Graphics) {
      val g2d = SwingUtils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.menuBarBorder())
      g2d.drawLine(0, getHeight - 1, getWidth, getHeight - 1)
    }

    def syncTheme {
      getComponents.foreach(_ match {
        case ts: ThemeSync => ts.syncTheme()
        case _ =>
      })
    }
  }

  private class SyncedCommandMenu(name: String) extends CommandMenu(name) with ThemeSync {
    private val menuUI = new BasicMenuUI with ThemeSync {
      override def syncTheme(): Unit = {
        setForeground(InterfaceColors.toolbarText())

        selectionBackground = InterfaceColors.menuBackgroundHover()
        selectionForeground = InterfaceColors.menuTextHover()
        acceleratorForeground = InterfaceColors.toolbarText()
        acceleratorSelectionForeground = InterfaceColors.menuTextHover()
        disabledForeground = InterfaceColors.menuTextDisabled()
      }
    }

    setUI(menuUI)
    syncTheme()

    override def addMenuItem(command: Command, menuItem: JMenuItem) {
      super.addMenuItem(command, new MenuItem(new AbstractAction(command.name) {
        def actionPerformed(e: ActionEvent) {
          command.execute()
        }
      }))
    }

    override def getPopupMenu: JPopupMenu = {
      val menu = super.getPopupMenu

      menu.setBackground(InterfaceColors.menuBackground())
      menu.setBorder(new LineBorder(InterfaceColors.menuBorder()))

      menu
    }

    override def syncTheme(): Unit = {
      menuUI.syncTheme()

      getMenuComponents.foreach(_ match {
        case ts: ThemeSync => ts.syncTheme()
        case _ =>
      })
    }
  }
}
