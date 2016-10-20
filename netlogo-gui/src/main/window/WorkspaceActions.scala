// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Toolkit
import java.awt.event.{ ActionEvent, InputEvent, KeyEvent }

import javax.swing.{ AbstractAction, Action, KeyStroke }

import org.nlogo.core.{ I18N, AgentKind }
import org.nlogo.api.Refreshable
import org.nlogo.swing.UserAction._

object WorkspaceActions {
  implicit val i18nName = I18N.Prefix("menu.tools")

  val HaltGroup     = "org.nlogo.window.WorkspaceActions.Halt"

  def apply(workspace: GUIWorkspace): Seq[Action] = {
    Seq(
      new SimpleGUIWorkspaceAction(I18N.gui("halt"), HaltGroup, workspace, _.halt),
      new SimpleGUIWorkspaceAction(I18N.gui("globalsMonitor"), ToolsMonitorGroup, workspace, _.inspectAgent(AgentKind.Observer)),
      new SimpleGUIWorkspaceAction(I18N.gui("turtleMonitor"), ToolsMonitorGroup, workspace, _.inspectAgent(AgentKind.Turtle)),
      new SimpleGUIWorkspaceAction(I18N.gui("patchMonitor"), ToolsMonitorGroup, workspace, _.inspectAgent(AgentKind.Patch)),
      new SimpleGUIWorkspaceAction(I18N.gui("linkMonitor"), ToolsMonitorGroup, workspace, _.inspectAgent(AgentKind.Link)),
      new SimpleGUIWorkspaceAction(I18N.gui("closeAllAgentMonitors"), ToolsMonitorGroup, workspace, _.closeAgentMonitors),
      new SimpleGUIWorkspaceAction(I18N.gui("closeDeadAgentMonitors"), ToolsMonitorGroup, workspace, _.stopInspectingDeadAgents),
      new Open3DViewAction(workspace),
      new SnapToGridAction(workspace))

  }

  class GUIWorkspaceAction(name: String, workspace: GUIWorkspace) extends AbstractAction(name) {
    def performAction(workspace: GUIWorkspace): Unit = {}

    override def actionPerformed(e: ActionEvent): Unit = {
      performAction(workspace)
    }
  }

  class SimpleGUIWorkspaceAction(name: String, group: String, workspace: GUIWorkspace, action: GUIWorkspace => Unit) extends GUIWorkspaceAction(name, workspace) {
    putValue(ActionCategoryKey, ToolsCategory)
    putValue(ActionGroupKey,    group)

    override def performAction(workspace: GUIWorkspace): Unit = {
      action(workspace)
    }
  }

  // this should be unified with switchTo3DViewAction in GUIWorkspace at some point...
  class Open3DViewAction(workspace: GUIWorkspace) extends GUIWorkspaceAction(I18N.gui.get("menu.tools.3DView.switch"), workspace) {
    putValue(ActionCategoryKey,      ToolsCategory)
    putValue(ActionGroupKey,         ToolsDialogsGroup)
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit.getMenuShortcutKeyMask | InputEvent.SHIFT_MASK))

    override def performAction(workspace: GUIWorkspace): Unit = {
      try {
        workspace.glView.open()
        workspace.set2DViewEnabled(false)
      }
      catch {
        case ex: org.nlogo.window.JOGLLoadingException =>
          org.nlogo.swing.Utils.alert("3d", ex.getMessage, "" + ex.getCause, I18N.gui.get("common.buttons.continue") )
      }
    }
  }
}

class HubNetControlCenterAction(workspace: GUIWorkspace) extends AbstractAction(I18N.gui.get("menu.tools.hubNetControlCenter")) {
    putValue(ActionCategoryKey,      ToolsCategory)
    putValue(ActionGroupKey,         ToolsHubNetGroup)
    putValue(Action.ACCELERATOR_KEY,
      KeyStroke.getKeyStroke(Character.valueOf('H'), Toolkit.getDefaultToolkit.getMenuShortcutKeyMask | InputEvent.SHIFT_MASK))

    override def actionPerformed(e: ActionEvent): Unit = {
      workspace.hubNetManager.get.showControlCenter
    }
}

class SnapToGridAction(workspace: GUIWorkspace)
  extends AbstractAction(I18N.gui.get("menu.edit.snapToGrid"))
  with CheckBoxAction
  with Refreshable {

  putValue(ActionCategoryKey,      EditCategory)
  putValue(ActionGroupKey,         "SnapToGrid")
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
