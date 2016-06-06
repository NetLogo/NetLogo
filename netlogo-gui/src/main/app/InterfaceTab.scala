// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.core.I18N
import org.nlogo.window.{InterfaceColors, WidgetInfo, EditDialogFactoryInterface, GUIWorkspace}
import org.nlogo.swing.Implicits.thunk2action
import javax.swing.{ BorderFactory, JScrollPane, ScrollPaneConstants, Action, ImageIcon, AbstractAction, JSplitPane, JPanel}
import java.awt.{Graphics2D, Graphics, Component, Container, ContainerOrderFocusTraversalPolicy, Dimension, BorderLayout}

class InterfaceTab(workspace: GUIWorkspace,
                   monitorManager: AgentMonitorManager,
                   dialogFactory: EditDialogFactoryInterface) extends JPanel
  with org.nlogo.window.Events.LoadBeginEvent.Handler
  with org.nlogo.window.Events.OutputEvent.Handler
  with org.nlogo.window.Events.ExportOutputEvent.Handler
  with org.nlogo.window.Events.Enable2DEvent.Handler
  with Events.SwitchedTabsEvent.Handler
  with org.nlogo.swing.Printable {

  setFocusCycleRoot(true)
  setFocusTraversalPolicy(new InterfaceTabFocusTraversalPolicy)
  val commandCenter = new CommandCenter(workspace, new CommandCenterLocationToggleAction)
  val iP = new InterfacePanel(workspace.viewWidget, workspace)
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
        add(new org.nlogo.swing.ToolBar.Separator)
        viewUpdatePanel = new org.nlogo.window.ViewUpdatePanel(workspace, workspace.viewWidget.displaySwitch, true, workspace.viewWidget.tickCounter)
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
    if(iP.isFocusable && splitPane.getDividerLocation >= maxDividerLocation) iP.requestFocus()
    else if(commandCenter != null) commandCenter.requestFocus()
  }

  final def handle(e: Events.SwitchedTabsEvent) {
    commandCenterAction.setEnabled(e.newTab == this)
  }

  def handle(e: org.nlogo.window.Events.LoadBeginEvent) {
    scrollPane.getHorizontalScrollBar.setValue(0)
    scrollPane.getVerticalScrollBar.setValue(0)
  }

  /// output

  def getOutputArea = Option(iP.getOutputWidget).map(_.outputArea).getOrElse(commandCenter.output)

  def handle(e: org.nlogo.window.Events.ExportOutputEvent) { getOutputArea.export(e.filename) }

  def handle(e: org.nlogo.window.Events.OutputEvent) {
    val outputArea = if(e.toCommandCenter) commandCenter.output else getOutputArea
    if(e.clear && iP.getOutputWidget != null) outputArea.clear()
    if(e.outputObject != null) outputArea.append(e.outputObject, e.wrapLines)
  }

  def handle(e: org.nlogo.window.Events.Enable2DEvent) {
    viewUpdatePanel.setVisible(e.enabled)
    viewUpdatePanel.handle(null)
  }

  /// printing

  // satisfy org.nlogo.swing.Printable
  override def print(g: Graphics, pageFormat: java.awt.print.PageFormat,
                     pageIndex: Int, printer: org.nlogo.swing.PrinterManager) =
    // only allow printing on 1 page since printing graphics over multiple pages would require a lot
    // more changes to the NetLogo source code --mag 10/23/02
    if(pageIndex > 0) java.awt.print.Printable.NO_SUCH_PAGE
    else {
      val g2d = g.asInstanceOf[Graphics2D]
      g2d.translate(pageFormat.getImageableX, pageFormat.getImageableY)
      iP.printAll(g2d)
      java.awt.print.Printable.PAGE_EXISTS
    }

  /// command center stuff

  private class CommandCenterLocationToggleAction extends AbstractAction("Toggle") {
    putValue(Action.SMALL_ICON,new ImageIcon(classOf[InterfaceTab].getResource("/images/toggle.gif")))
    override def actionPerformed(e: java.awt.event.ActionEvent) {
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

  val commandCenterAction = {
    implicit val i18nPrefix = I18N.Prefix("menu.tools")
    new AbstractAction(I18N.gui("hideCommandCenter")) {
      override def actionPerformed(e: java.awt.event.ActionEvent) {
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
