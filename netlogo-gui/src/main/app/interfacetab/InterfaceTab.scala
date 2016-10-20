// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{BorderLayout, Component, Container, ContainerOrderFocusTraversalPolicy, Dimension, Graphics, Graphics2D, KeyboardFocusManager, Toolkit}
import java.awt.event.ActionEvent
import java.awt.print.{PageFormat, Printable}
import javax.swing._

import org.nlogo.app.common.{Events => AppEvents}
import org.nlogo.app.tools.AgentMonitorManager
import org.nlogo.core.I18N
import org.nlogo.swing.{PrinterManager, ToolBar, Printable => NlogoPrintable, UserAction },
  UserAction.{ ActionCategoryKey, ActionGroupKey, ToolsCategory }
import org.nlogo.swing.Implicits.thunk2action
import org.nlogo.window.{EditDialogFactoryInterface, GUIWorkspace, InterfaceColors, ViewUpdatePanel, WidgetInfo, Events => WindowEvents}


object InterfaceTab {
  val MenuGroup = "org.nlogo.app.InterfaceTab"
}

import InterfaceTab._

class InterfaceTab(workspace: GUIWorkspace,
                   monitorManager: AgentMonitorManager,
                   dialogFactory: EditDialogFactoryInterface) extends JPanel
  with WindowEvents.LoadBeginEvent.Handler
  with WindowEvents.OutputEvent.Handler
  with WindowEvents.Enable2DEvent.Handler
  with AppEvents.SwitchedTabsEvent.Handler
  with NlogoPrintable {

  setFocusCycleRoot(true)
  setFocusTraversalPolicy(new InterfaceTabFocusTraversalPolicy)
  val commandCenter = new CommandCenter(workspace, new CommandCenterLocationToggleAction)
  val iP = new InterfacePanel(workspace.viewWidget, workspace)
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

  private var viewUpdatePanel: org.nlogo.window.ViewUpdatePanel = null

  private val splitPane = new JSplitPane(
    JSplitPane.VERTICAL_SPLIT,
    true, // continuous layout as the user drags
    scrollPane, commandCenter)
  splitPane.setOneTouchExpandable(true)
  splitPane.setResizeWeight(1) // give the InterfacePanel all
  add(splitPane, BorderLayout.CENTER)
  locally {
    import WidgetInfo._
    val buttons = List(button, slider, switch, chooser, input, monitor, plot, output, note)
    add(new InterfaceToolBar(iP, workspace, buttons, workspace.getFrame, dialogFactory) {
      override def addControls() {
        super.addControls()
        add(new ToolBar.Separator)
        viewUpdatePanel = new ViewUpdatePanel(workspace, workspace.viewWidget.displaySwitch, workspace.viewWidget.tickCounter)
        add(viewUpdatePanel)
      }
    }, BorderLayout.NORTH)
  }

  org.nlogo.swing.Utils.addEscKeyAction(this, () => InterfaceTab.this.monitorManager.closeTopMonitor())

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
    if(iP.isFocusable && splitPane.getDividerLocation >= maxDividerLocation) {
      iP.requestFocusInWindow()
    }
  }

  final def handle(e: AppEvents.SwitchedTabsEvent) {
    if (e.newTab != this) {
      lastFocusedComponent = if(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == commandCenter.commandLine.textField)
        commandCenter else iP

      monitorManager.refresh()
    } else {
      commandCenterAction.setEnabled(e.newTab == this)
      lastFocusedComponent.requestFocus()
    }
  }

  def handle(e: WindowEvents.LoadBeginEvent) {
    scrollPane.getHorizontalScrollBar.setValue(0)
    scrollPane.getVerticalScrollBar.setValue(0)
  }

  /// output

  def getOutputArea = Option(iP.getOutputWidget).map(_.outputArea).getOrElse(commandCenter.output)

  def handle(e: WindowEvents.OutputEvent) {
    val outputArea = if(e.toCommandCenter) commandCenter.output else getOutputArea
    if(e.clear && iP.getOutputWidget != null) outputArea.clear()
    if(e.outputObject != null) outputArea.append(e.outputObject, e.wrapLines)
  }

  def handle(e: WindowEvents.Enable2DEvent) {
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
    putValue(Action.SMALL_ICON,new ImageIcon(classOf[InterfaceTab].getResource("/images/toggle.gif")))
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

  def menuActions: Seq[Action] = Seq(commandCenterAction)

  val commandCenterAction = {
    implicit val i18nPrefix = I18N.Prefix("menu.tools")
    new AbstractAction(I18N.gui("hideCommandCenter")) {
      putValue(ActionCategoryKey, ToolsCategory)
      putValue(ActionGroupKey,    MenuGroup)
      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(Character.valueOf('/'), Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))

      override def actionPerformed(e: ActionEvent) {
        if (splitPane.getDividerLocation < maxDividerLocation) {
          splitPane.setDividerLocation(maxDividerLocation)
          if (iP.isFocusable) iP.requestFocus()
        }
        else {
          if (splitPane.getLastDividerLocation < maxDividerLocation)
            splitPane.setDividerLocation(splitPane.getLastDividerLocation)
          else // the window must have been resized.  oh well, hope for the best... - ST 11/12/04
            splitPane.getOrientation match {
              case JSplitPane.VERTICAL_SPLIT => splitPane.resetToPreferredSizes()
              case _ => // horizontal
                // dunno why, but resetToPreferredSizes() doesn't work - ST 11/12/04
                splitPane.setDividerLocation(0.5)
            }
          commandCenter.requestFocus()
        }
        putValue(Action.NAME,
          if (splitPane.getDividerLocation < maxDividerLocation) I18N.gui("hideCommandCenter")
          else I18N.gui("showCommandCenter"))
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
