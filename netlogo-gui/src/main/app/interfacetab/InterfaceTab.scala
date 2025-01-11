// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ BorderLayout, Component, Container, ContainerOrderFocusTraversalPolicy, Dimension, Graphics,
                  Graphics2D }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener }
import java.awt.print.{ PageFormat, Printable }
import javax.swing.{ AbstractAction, Action, JComponent, JPanel, JSplitPane, ScrollPaneConstants }

import org.nlogo.app.common.{Events => AppEvents, MenuTab}, AppEvents.SwitchedTabsEvent
import org.nlogo.app.tools.AgentMonitorManager
import org.nlogo.core.I18N
import org.nlogo.swing.{ Implicits, PrinterManager, Printable => NlogoPrintable, ScrollPane, ToolBar, UserAction,
                         Utils },
                       Implicits.thunk2action, UserAction.{ MenuAction, ToolsCategory }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ EditDialogFactoryInterface, GUIWorkspace, SpeedSliderPanel, ViewUpdatePanel, WidgetInfo,
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
  private val locationToggleAction = new CommandCenterLocationToggleAction
  commandCenter.locationToggleAction = locationToggleAction
  val iP = new InterfacePanel(workspace.viewWidget, workspace)

  val commandCenterToggleAction = new CommandCenterToggleAction()

  override val activeMenuActions =
    WorkspaceActions.interfaceActions(workspace) ++
    Seq(iP.undoAction, iP.redoAction, commandCenterToggleAction, new JumpToCommandCenterAction())

  var lastFocusedComponent: JComponent = commandCenter
  setLayout(new BorderLayout)
  private val scrollPane = new ScrollPane(
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

  private val widgetControls = {
    import WidgetInfo._

    val buttons = List(button, slider, switch, chooser, input, monitor, plot, output, note)

    new InterfaceWidgetControls(iP, workspace, buttons, workspace.getFrame, dialogFactory)
  }

  private val speedSlider = new SpeedSliderPanel(workspace, workspace.viewWidget.tickCounter)

  private val viewUpdatePanel = new ViewUpdatePanel(workspace, speedSlider, workspace.viewWidget.displaySwitch,
                                                    workspace.viewWidget.tickCounter)

  private val splitPane = new SplitPane(scrollPane, commandCenter, commandCenterToggleAction)

  add(splitPane, BorderLayout.CENTER)

  object TrackingFocusListener extends FocusListener {
    var lastFocused = Option.empty[Component]
    override def focusGained(e: FocusEvent): Unit = {
      lastFocused = Some(e.getSource.asInstanceOf[Component])
    }
    override def focusLost(e: FocusEvent): Unit = { }
  }

  private val toolBar = new DynamicToolbar(widgetControls, speedSlider, viewUpdatePanel)

  add(toolBar, BorderLayout.NORTH)

  iP.addFocusListener(TrackingFocusListener)

  commandCenter.getDefaultComponentForFocus.addFocusListener(TrackingFocusListener)

  Utils.addEscKeyAction(this, () => InterfaceTab.this.monitorManager.closeTopMonitor())

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
    speedSlider.setVisible(e.enabled)
    viewUpdatePanel.setVisible(e.enabled)
    viewUpdatePanel.handle(null)
    revalidate()
    repaint()
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

  class DynamicToolbar(widgetControls: InterfaceWidgetControls, speedSlider: SpeedSliderPanel,
                       viewUpdatePanel: ViewUpdatePanel)
    extends ToolBar with ThemeSync {

    setLayout(null)

    override def getPreferredSize: Dimension =
      new Dimension(super.getPreferredSize.width, widgetControls.getPreferredSize.height.
                                                  max(speedSlider.getPreferredSize.height).
                                                  max(viewUpdatePanel.getPreferredSize.height) + 16)

    override def addControls() {
      add(widgetControls)
      add(speedSlider)
      add(viewUpdatePanel)
    }

    override def doLayout() {
      if (speedSlider.isVisible) {
        val left = (getWidth / 2 - speedSlider.getPreferredSize.width / 2 -
                    widgetControls.getPreferredSize.width - 180).max(0)

        widgetControls.setBounds(left, getHeight / 2 - widgetControls.getPreferredSize.height / 2,
                                 widgetControls.getPreferredSize.width, widgetControls.getPreferredSize.height)
        speedSlider.setBounds((getWidth / 2 - speedSlider.getPreferredSize.width / 2).
                              max(left + widgetControls.getWidth + 40),
                              getHeight / 2 - speedSlider.getPreferredSize.height / 2,
                              speedSlider.getPreferredSize.width, speedSlider.getPreferredSize.height)
        viewUpdatePanel.setBounds(speedSlider.getX + speedSlider.getWidth +
                                  speedSlider.getX - (widgetControls.getX + widgetControls.getWidth),
                                  getHeight / 2 - viewUpdatePanel.getPreferredSize.height / 2,
                                  viewUpdatePanel.getPreferredSize.width, viewUpdatePanel.getPreferredSize.height)
      }

      else {
        widgetControls.setBounds(0, getHeight / 2 - widgetControls.getPreferredSize.height / 2,
                                 widgetControls.getPreferredSize.width, widgetControls.getPreferredSize.height)
      }
    }

    override def paintComponent(g: Graphics) {
      super.paintComponent(g)

      val gap = speedSlider.getX - (widgetControls.getX + widgetControls.getWidth)

      if (speedSlider.isVisible && gap <= 80) {
        val g2d = Utils.initGraphics2D(g)

        g2d.setColor(InterfaceColors.TOOLBAR_SEPARATOR)
        g2d.fillRect(speedSlider.getX - gap / 2, getY + 8, 1, getHeight - 16)
        g2d.fillRect(viewUpdatePanel.getX - gap / 2, getY + 8, 1, getHeight - 16)
      }
    }

    def syncTheme() {
      setBackground(InterfaceColors.TOOLBAR_BACKGROUND)

      widgetControls.syncTheme()
      viewUpdatePanel.syncTheme()
    }
  }

  /// command center stuff

  private class CommandCenterLocationToggleAction extends AbstractAction with ThemeSync {
    override def actionPerformed(e: ActionEvent) {
      splitPane.getOrientation match {
        case JSplitPane.VERTICAL_SPLIT => splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT)
        case JSplitPane.HORIZONTAL_SPLIT => splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT)
      }

      splitPane.resetToPreferredSizes()

      syncTheme()
    }

    def syncTheme() {
      splitPane.getOrientation match {
        case JSplitPane.VERTICAL_SPLIT =>
          putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/shift-bottom.png", 10, 10,
                                                                InterfaceColors.LOCATION_TOGGLE_IMAGE))
        case JSplitPane.HORIZONTAL_SPLIT =>
          putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/shift-right.png", 10, 10,
                                                                InterfaceColors.LOCATION_TOGGLE_IMAGE))
      }
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
    iP.syncTheme()

    scrollPane.setBackground(InterfaceColors.INTERFACE_BACKGROUND)

    commandCenter.syncTheme()
    locationToggleAction.syncTheme()
  }
}
