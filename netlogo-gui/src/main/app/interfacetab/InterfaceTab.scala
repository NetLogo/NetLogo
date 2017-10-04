// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ BorderLayout, Component, Container,
  ContainerOrderFocusTraversalPolicy, Dimension, Graphics, Graphics2D }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener }
import java.awt.print.{ PageFormat, Printable }
import javax.swing.{ AbstractAction, Action, BorderFactory, JComponent,
  JPanel, JScrollPane, JSplitPane, ScrollPaneConstants }

import org.nlogo.app.common.{Events => AppEvents, MenuTab}, AppEvents.SwitchedTabsEvent
import org.nlogo.app.tools.AgentMonitorManager
import org.nlogo.core.I18N
import org.nlogo.swing.{PrinterManager, ToolBar, Printable => NlogoPrintable, UserAction, Utils },
  UserAction.{ MenuAction, ToolsCategory },
  Utils.icon
import org.nlogo.swing.{ Implicits, Utils => SwingUtils }, Implicits.thunk2action
import org.nlogo.window.{ EditDialogFactoryInterface, GUIWorkspace,
  InterfaceColors, OutputArea, ViewUpdatePanel, WidgetInfo,
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
  with MenuTab {

  setFocusCycleRoot(true)
  setFocusTraversalPolicy(new InterfaceTabFocusTraversalPolicy)
  commandCenter.locationToggleAction = new CommandCenterLocationToggleAction
  val iP = new InterfacePanel(workspace.viewWidget, workspace)

  activeMenuActions =
    WorkspaceActions.interfaceActions(workspace) ++
    Seq(iP.undoAction, iP.redoAction, new CommandCenterToggleAction(), new JumpToCommandCenterAction())

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
  scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, InterfaceColors.GRAPHICS_BACKGROUND))
  commandCenter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, InterfaceColors.GRAPHICS_BACKGROUND))
  commandCenter.setMinimumSize(new Dimension(0, 0))

  private var viewUpdatePanel: ViewUpdatePanel = null

  private val splitPane = new JSplitPane(
    JSplitPane.VERTICAL_SPLIT,
    true, // continuous layout as the user drags
    scrollPane, commandCenter)
  splitPane.setOneTouchExpandable(true)
  splitPane.setResizeWeight(1) // give the InterfacePanel all
  add(splitPane, BorderLayout.CENTER)

  object TrackingFocusListener extends FocusListener {
    var lastFocused = Option.empty[Component]
    override def focusGained(e: FocusEvent): Unit = {
      lastFocused = Some(e.getSource.asInstanceOf[Component])
    }
    override def focusLost(e: FocusEvent): Unit = { }
  }

  locally {
    import WidgetInfo._
    val buttons = List(button, slider, switch, chooser, input, monitor, plot, output, note)
    add(new InterfaceToolBar(iP, workspace, buttons, workspace.getFrame, dialogFactory) {
      override def addControls() {
        super.addControls()
        add(new ToolBar.Separator)
        viewUpdatePanel = new ViewUpdatePanel(workspace, workspace.viewWidget.tickCounter)
        add(viewUpdatePanel)
      }
    }, BorderLayout.NORTH)
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

  override def requestFocus() {
    TrackingFocusListener.lastFocused.getOrElse(commandCenter).requestFocusInWindow()
  }

  final def handle(e: SwitchedTabsEvent) {
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

  def getOutputArea: Option[OutputArea] =
    Option(iP.getOutputWidget).flatMap(_.outputArea) orElse commandCenter.outputArea

  def handle(e: OutputEvent) {
    val outputArea = if (e.toCommandCenter) commandCenter.outputArea else getOutputArea
    if(e.clear && iP.getOutputWidget != null) outputArea.foreach(_.clear())
    if(e.outputObject != null) outputArea.foreach(_.append(e.outputObject, e.wrapLines))
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

  private class CommandCenterLocationToggleAction extends AbstractAction("Toggle") {
    putValue(Action.SMALL_ICON, icon("/images/toggle.gif"))
    override def actionPerformed(e: ActionEvent) {
      splitPane.getOrientation match {
        case JSplitPane.VERTICAL_SPLIT =>
          splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT)
          // dunno why, but resetToPreferredSizes() doesn't do the right thing here - ST 11/12/04
          splitPane.setDividerLocation(0.5)
        case _ => // horizontal
          splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT)
          splitPane.resetToPreferredSizes()
      }
    }
  }

  private def showCommandCenter(): Unit = {
    if (splitPane.getLastDividerLocation < maxDividerLocation)
      splitPane.setDividerLocation(splitPane.getLastDividerLocation)
    else // the window must have been resized.  oh well, hope for the best... - ST 11/12/04
      splitPane.getOrientation match {
        case JSplitPane.VERTICAL_SPLIT => splitPane.resetToPreferredSizes()
        case _ => // horizontal
          // dunno why, but resetToPreferredSizes() doesn't work - ST 11/12/04
          splitPane.setDividerLocation(0.5)
      }
  }

  class CommandCenterToggleAction extends AbstractAction(I18N.gui.get("menu.tools.hideCommandCenter"))
  with MenuAction {
    category    = ToolsCategory
    group       = MenuGroup
    accelerator = UserAction.KeyBindings.keystroke('/', withMenu = true)

    override def actionPerformed(e: ActionEvent) {
      if (splitPane.getDividerLocation < maxDividerLocation) {
        splitPane.setDividerLocation(maxDividerLocation)
        if (iP.isFocusable) iP.requestFocus()
      } else {
        showCommandCenter()
        commandCenter.requestFocus()
      }
      putValue(Action.NAME,
        if (splitPane.getDividerLocation < maxDividerLocation) I18N.gui.get("menu.tools.hideCommandCenter")
        else I18N.gui.get("menu.tools.showCommandCenter"))
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

  private def maxDividerLocation =
    if(splitPane.getOrientation == JSplitPane.VERTICAL_SPLIT)
      splitPane.getHeight - splitPane.getDividerSize - splitPane.getInsets.top
    else splitPane.getWidth - splitPane.getDividerSize - splitPane.getInsets.left

  // respect the size of the command center when loading and normalizing
  def adjustTargetSize(targetSize: Dimension) {
    if(splitPane.getOrientation == JSplitPane.HORIZONTAL_SPLIT)
      targetSize.width += commandCenter.getSize().width - commandCenter.getPreferredSize.width
    else targetSize.height += commandCenter.getSize().height - commandCenter.getPreferredSize.height
  }
}
