// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.event.{ ActionEvent, KeyEvent }

import javax.swing.{ AbstractAction, Action }

import org.nlogo.api.{ Refreshable, Version }
import org.nlogo.core.{ AgentKind, LocaleContext }
import org.nlogo.swing.UserAction._

object WorkspaceActions {
  val HaltGroup     = "org.nlogo.window.WorkspaceActions.Halt"

  def apply(workspace: GUIWorkspace): Seq[MenuAction] =
    Seq(
      new HaltAction(workspace),
      new SimpleGUIWorkspaceAction(loc"Globals Monitor", ToolsMonitorGroup, workspace, _.inspectAgent(AgentKind.Observer)) {
        mnemonic = 'G'
      },
      new SimpleGUIWorkspaceAction(loc"Turtle Monitor", ToolsMonitorGroup, workspace, _.inspectAgent(AgentKind.Turtle)) {
        mnemonic = 'T'
      },
      new SimpleGUIWorkspaceAction(loc"Patch Monitor", ToolsMonitorGroup, workspace, _.inspectAgent(AgentKind.Patch)) {
        mnemonic = 'O'
      },
      new SimpleGUIWorkspaceAction(loc"Link Monitor", ToolsMonitorGroup, workspace, _.inspectAgent(AgentKind.Link)) {
        mnemonic = 'L'
      },
      new SimpleGUIWorkspaceAction(loc"Close All Agent Monitors", ToolsMonitorGroup, workspace, _.closeAgentMonitors()) {
        mnemonic = 'A'
      },
      new SimpleGUIWorkspaceAction(loc"Close Monitors for Dead Agents", ToolsMonitorGroup, workspace, _.stopInspectingDeadAgents()) {
        mnemonic = 'M'
      },
    ) ++ (if (Version.is3D) Seq() else Seq(new Open3DViewAction(workspace)))

  def interfaceActions(workspace: GUIWorkspace, widgetPanel: AbstractWidgetPanel): Seq[MenuAction] =
    Seq(new SnapToGridAction(workspace, widgetPanel))

  class GUIWorkspaceAction(name: String, workspace: GUIWorkspace) extends AbstractAction(name) with MenuAction {
    def performAction(workspace: GUIWorkspace): Unit = {}

    override def actionPerformed(e: ActionEvent): Unit = {
      performAction(workspace)
    }
  }

  class SimpleGUIWorkspaceAction(name: String, menuGroup: String, workspace: GUIWorkspace, action: GUIWorkspace => Unit)
    extends GUIWorkspaceAction(name, workspace) {

    category = ToolsCategory
    group    = menuGroup

    override def performAction(workspace: GUIWorkspace): Unit = {
      action(workspace)
    }
  }

  class HaltAction(workspace: GUIWorkspace)
    extends SimpleGUIWorkspaceAction(loc"Halt", HaltGroup, workspace, _.halt) {

    mnemonic = 'H'

    override def isEnabled: Boolean =
      workspace.jobManager.anyPrimaryJobs()
  }

  // this should be unified with switchTo3DViewAction in GUIWorkspace at some point...
  class Open3DViewAction(workspace: GUIWorkspace)
  extends GUIWorkspaceAction(loc"Switch to 3D View", workspace) {
    category    = ToolsCategory
    group       = ToolsDialogsGroup
    rank        = 0
    accelerator = KeyBindings.keystroke('T', withMenu = true, withShift = true)
    mnemonic    = KeyEvent.VK_3

    override def performAction(workspace: GUIWorkspace): Unit = {
      if (!workspace.isTesting) {
        try {
          workspace.glView.open()
          workspace.set2DViewEnabled(false)
        } catch {
          case ex: org.nlogo.window.JOGLLoadingException =>
            org.nlogo.swing.Utils.alert("3d", ex.getMessage, "" + ex.getCause, loc"Continue")
        }
      }
    }
  }
}

class HubNetControlCenterAction(workspace: GUIWorkspace)
  extends AbstractAction(loc"HubNet Control Center")
  with MenuAction {
    category    = ToolsCategory
    group       = ToolsHubNetGroup
    accelerator = KeyBindings.keystroke('H', withMenu = true, withShift = true)

    override def actionPerformed(e: ActionEvent): Unit = {
      workspace.hubNetManager.get.showControlCenter(false)
    }
}

class SnapToGridAction(workspace: GUIWorkspace, widgetPanel: AbstractWidgetPanel)
  extends AbstractAction(loc"Snap to Grid")
  with CheckBoxAction
  with MenuAction
  with Refreshable {

  category = EditCategory
  group    = "SnapToGrid"
  mnemonic = 'S'

  putValue(Action.SELECTED_KEY,    checkedState)

  def actionPerformed(e: ActionEvent) = {
    workspace.setSnapOn(! workspace.snapOn)
    putValue(Action.SELECTED_KEY, checkedState)
  }

  def checkedState: Boolean = workspace.snapOn

  def refresh(): Unit = {
    putValue(Action.SELECTED_KEY, checkedState)
  }
}
