// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ BorderLayout, Component, Container, ContainerOrderFocusTraversalPolicy, Dimension, Graphics,
                  Graphics2D, GridBagConstraints }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener }
import java.awt.print.{ PageFormat, Printable }
import javax.swing.{ AbstractAction, Action, JComponent, JPanel, JScrollPane, JSplitPane, ScrollPaneConstants }

import org.nlogo.app.common.{Events => AppEvents, MenuTab}, AppEvents.SwitchedTabsEvent
import org.nlogo.app.tools.AgentMonitorManager
import org.nlogo.core.I18N
import org.nlogo.swing.{ Implicits, PrinterManager, Printable => NlogoPrintable, UserAction, Utils },
                       Implicits.thunk2action, UserAction.{ MenuAction, ToolsCategory }
import org.nlogo.swing.{ Utils => SwingUtils }
import org.nlogo.window.{ EditDialogFactoryInterface, GUIWorkspace, ThemeSync, ViewUpdatePanel, WidgetInfo,
                          Events => WindowEvents, WorkspaceActions },
                        WindowEvents.{ Enable2DEvent, LoadBeginEvent, OutputEvent }

object InterfaceTab {
  val MenuGroup = "org.nlogo.app.InterfaceTab"
}

import InterfaceTab._

class InterfaceTab(workspace: GUIWorkspace,
                   monitorManager: AgentMonitorManager,
                   dialogFactory: EditDialogFactoryInterface,
                   val commandCenter: CommandCenter) extends JPanel
  with LoadBeginEvent.Handler
  with OutputEvent.Handler
  with Enable2DEvent.Handler
  with SwitchedTabsEvent.Handler
  with NlogoPrintable
  with MenuTab
  with ThemeSync {

  setFocusCycleRoot(true)
  setFocusTraversalPolicy(new InterfaceTabFocusTraversalPolicy)
  commandCenter.locationToggleAction = new CommandCenterLocationToggleAction
  val iP = new InterfacePanel(workspace.viewWidget, workspace)

  val commandCenterToggleAction = new CommandCenterToggleAction()

  override val activeMenuActions =
    WorkspaceActions.interfaceActions(workspace) ++
    Seq(iP.undoAction, iP.redoAction, commandCenterToggleAction, new JumpToCommandCenterAction())

  var lastFocusedComponent: JComponent = commandCenter
  setLayout(new BorderLayout)
  private val scrollPane = new JScrollPane(
    iP,
    // always reserve space for the vertical scrollbar, otherwise when it appears it causes a
    // horizontal scrollbar to appear too!  this problem is especially noticeable now that we have
    // the JSplitPane, which means the user is very likely to resize the InterfacePanel in such a
    // way so that only a vertical scrollbar is really needed - ST 7/13/04
    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)

  scrollPane.setBorder(null)

  if (System.getProperty("os.name").startsWith("Windows")) {
    scrollPane.getVerticalScrollBar.setPreferredSize(new Dimension(7, 0))
    scrollPane.getHorizontalScrollBar.setPreferredSize(new Dimension(0, 7))
  }

  else {
    scrollPane.getVerticalScrollBar.setPreferredSize(new Dimension(10, 0))
    scrollPane.getHorizontalScrollBar.setPreferredSize(new Dimension(0, 10))
  }

  commandCenter.setMinimumSize(new Dimension(0, 0))

  private var viewUpdatePanel: ViewUpdatePanel = null

  private val splitPane = new SplitPane(scrollPane, commandCenter, commandCenterToggleAction)

  add(splitPane, BorderLayout.CENTER)

  object TrackingFocusListener extends FocusListener {
    var lastFocused = Option.empty[Component]
    override def focusGained(e: FocusEvent): Unit = {
      lastFocused = Some(e.getSource.asInstanceOf[Component])
    }
    override def focusLost(e: FocusEvent): Unit = { }
  }

  import WidgetInfo._

  private val buttons = List(button, slider, switch, chooser, input, monitor, plot, output, note)

  private val toolBar = new InterfaceToolBar(iP, workspace, buttons, workspace.getFrame, dialogFactory) {
    override def addControls() {
      super.addControls()
      viewUpdatePanel = new ViewUpdatePanel(workspace, workspace.viewWidget.displaySwitch,
                                            workspace.viewWidget.tickCounter)
      val c = new GridBagConstraints
      c.gridy = 0
      c.gridheight = 2
      add(viewUpdatePanel, c)
    }
  }

  locally {
    add(toolBar, BorderLayout.NORTH)
    iP.addFocusListener(TrackingFocusListener)
    commandCenter.getDefaultComponentForFocus.addFocusListener(TrackingFocusListener)
  }

  SwingUtils.addEscKeyAction(this, () => InterfaceTab.this.monitorManager.closeTopMonitor())

  private class InterfaceTabFocusTraversalPolicy extends ContainerOrderFocusTraversalPolicy {
    override def getComponentAfter(focusCycleRoot: Container, aComponent: Component) =
      if(aComponent == iP) commandCenter.getDefaultComponentForFocus
      else super.getComponentAfter(focusCycleRoot, aComponent)
    override def getComponentBefore(focusCycleRoot: Container, aComponent: Component) =
      if(aComponent == iP) commandCenter.getDefaultComponentForFocus
      else super.getComponentBefore(focusCycleRoot, aComponent)
  }

  def getInterfacePanel = iP

  // When we get focus, we want to focus the command center first
  // to prevent keyboard shortcuts (copy, cut, paste) from affecting
  // the code tab or wherever the cursor was before the user switched - RG 2/16/18
  override def requestFocus() {
    commandCenter.requestFocusInWindow()
    TrackingFocusListener.lastFocused.getOrElse(commandCenter).requestFocusInWindow()
  }

  final def handle(e: SwitchedTabsEvent) {
    commandCenter.requestFocusInWindow()
    TrackingFocusListener.lastFocused.getOrElse(commandCenter).requestFocusInWindow()
    if (e.newTab != this) {
      monitorManager.refresh()
    }
  }

  def handle(e: LoadBeginEvent) {
    scrollPane.getHorizontalScrollBar.setValue(0)
    scrollPane.getVerticalScrollBar.setValue(0)
  }

  /// output

  def getOutputArea = Option(iP.getOutputWidget).map(_.outputArea).getOrElse(commandCenter.output)

  def handle(e: OutputEvent) {
    val outputArea = if(e.toCommandCenter) commandCenter.output else getOutputArea
    if(e.clear && iP.getOutputWidget != null) outputArea.clear()
    if(e.outputObject != null) outputArea.append(e.outputObject, e.wrapLines)
  }

  def handle(e: Enable2DEvent) {
    viewUpdatePanel.setVisible(e.enabled)
    viewUpdatePanel.handle(null)
  }

  /// printing

  // satisfy org.nlogo.swing.Printable
  override def print(g: Graphics, pageFormat: PageFormat,
                     pageIndex: Int, printer: PrinterManager) =
    // only allow printing on 1 page since printing graphics over multiple pages would require a lot
    // more changes to the NetLogo source code --mag 10/23/02
    if(pageIndex > 0) Printable.NO_SUCH_PAGE
    else {
      val g2d = g.asInstanceOf[Graphics2D]
      g2d.translate(pageFormat.getImageableX, pageFormat.getImageableY)
      iP.printAll(g2d)
      Printable.PAGE_EXISTS
    }

  /// command center stuff

  private class CommandCenterLocationToggleAction extends AbstractAction {
    putValue(Action.SMALL_ICON, Utils.iconScaled("/images/shift-right.png", 10, 10))

    override def actionPerformed(e: ActionEvent) {
      splitPane.getOrientation match {
        case JSplitPane.VERTICAL_SPLIT =>
          splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT)
          putValue(Action.SMALL_ICON, Utils.iconScaled("/images/shift-right.png", 10, 10))
        case JSplitPane.HORIZONTAL_SPLIT =>
          splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT)
          putValue(Action.SMALL_ICON, Utils.iconScaled("/images/shift-bottom.png", 10, 10))
      }

      splitPane.resetToPreferredSizes()
    }
  }

  private def showCommandCenter(): Unit = {
    if (splitPane.getDividerLocation >= splitPane.maxDividerLocation)
      splitPane.resetToPreferredSizes()
  }

  class CommandCenterToggleAction extends AbstractAction(I18N.gui.get("menu.tools.hideCommandCenter"))
  with MenuAction {
    category    = ToolsCategory
    group       = MenuGroup
    accelerator = UserAction.KeyBindings.keystroke('/', withMenu = true)

    override def actionPerformed(e: ActionEvent) {
      if (splitPane.getDividerLocation < splitPane.maxDividerLocation) {
        splitPane.setDividerLocation(splitPane.maxDividerLocation)
        if (iP.isFocusable) iP.requestFocus()
      } else {
        showCommandCenter()
        commandCenter.requestFocus()
      }
    }
  }

  class JumpToCommandCenterAction extends AbstractAction(I18N.gui.get("menu.tools.jumpToCommandCenter"))
  with MenuAction {
    category    = ToolsCategory
    group       = MenuGroup
    accelerator = UserAction.KeyBindings.keystroke('C', withMenu = true, withShift = true)

    override def actionPerformed(e: ActionEvent) {
      if (! commandCenter.getDefaultComponentForFocus.isFocusOwner) {
        showCommandCenter()
        commandCenter.requestFocusInWindow()
      }
    }
  }

  def packSplitPane() {
    splitPane.setPreferredSize(
      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          new Dimension(scrollPane.getPreferredSize.width, scrollPane.getPreferredSize.height + splitPane.getDividerSize
                        + commandCenter.getPreferredSize.height)
        case JSplitPane.VERTICAL_SPLIT =>
          new Dimension(scrollPane.getPreferredSize.width + splitPane.getDividerSize +
                        commandCenter.getPreferredSize.width, scrollPane.getPreferredSize.height)
      })

    splitPane.revalidate()
  }

  def resetSplitPane() {
    splitPane.resetToPreferredSizes()
  }

  def syncTheme() {
    toolBar.syncTheme()
    viewUpdatePanel.syncTheme()
    iP.syncTheme()
    commandCenter.syncTheme()
  }
}
