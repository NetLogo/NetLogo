// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that
 each have their own menu bar and menus ev 8/25/05 */

import org.nlogo.core.AgentKind
import org.nlogo.core.I18N
import org.nlogo.window.GUIWorkspace

class ToolsMenu(app: App, modelSaver: ModelSaver) extends org.nlogo.swing.Menu(I18N.gui.get("menu.tools")) {
  implicit val i18nName = I18N.Prefix("menu.tools")

  setMnemonic('T')
  if (!System.getProperty("os.name").startsWith("Mac")) {
    addMenuItem(I18N.gui("preferences"), app.showPreferencesDialog _)
    addSeparator()
  }
  addMenuItem(new SimpleGUIWorkspaceAction(I18N.gui("halt"), app.workspace, _.halt))
  addSeparator()
  addMenuItem(new SimpleGUIWorkspaceAction(I18N.gui("globalsMonitor"), app.workspace, _.inspectAgent(AgentKind.Observer)))
  addMenuItem(new SimpleGUIWorkspaceAction(I18N.gui("turtleMonitor"), app.workspace, _.inspectAgent(AgentKind.Turtle)))
  addMenuItem(new SimpleGUIWorkspaceAction(I18N.gui("patchMonitor"), app.workspace, _.inspectAgent(AgentKind.Patch)))
  addMenuItem(new SimpleGUIWorkspaceAction(I18N.gui("linkMonitor"), app.workspace, _.inspectAgent(AgentKind.Link)))
  addMenuItem(new SimpleGUIWorkspaceAction(I18N.gui("closeAllAgentMonitors"), app.workspace, _.closeAgentMonitors))
  addMenuItem(new SimpleGUIWorkspaceAction(I18N.gui("closeDeadAgentMonitors"), app.workspace, _.stopInspectingDeadAgents))
  addSeparator()
  addMenuItem('/', app.tabs.interfaceTab.commandCenterAction)
  addSeparator()
  addMenuItem('T', new Open3DViewAction(app.workspace))
  addMenuItem(I18N.gui("colorSwatches"), openColorDialog _)
  addMenuItem(I18N.gui("turtleShapesEditor"),
              () => app.turtleShapesManager.init(I18N.gui("turtleShapesEditor")))
  addMenuItem(I18N.gui("linkShapesEditor"),
              () => app.linkShapesManager.init(I18N.gui("linkShapesEditor")))
  addMenuItem(app.previewCommandsEditor.title, 'P', true, () =>
    app.workspace.previewCommands =
      app.previewCommandsEditor.getPreviewCommands(modelSaver.currentModel, app.workspace.getModelPath))
  addMenuItem(I18N.gui("behaviorSpace"), 'B', true, () => app.labManager.show())
  addMenuItem(I18N.gui("systemDynamicsModeler"), 'D', true, app.aggregateManager.showEditor _)
  addSeparator()
  addMenuItem(I18N.gui("hubNetClientEditor"), openHubNetClientEditor _)
  addMenuItem('H', true, app.workspace.hubNetControlCenterAction)

  def openColorDialog(): Unit = {
    if(app.colorDialog == null) {
      app.colorDialog =
        new org.nlogo.window.ColorDialog(app.frame, false)
      org.nlogo.awt.Positioning.center(app.colorDialog, app.frame)
      app.colorDialog.showDialog()
    }
    else {
      org.nlogo.awt.Positioning.center(app.colorDialog, app.frame)
      app.colorDialog.setVisible(true)
    }
  }

  def openHubNetClientEditor(): Unit = {
    app.workspace.getHubNetManager.foreach { mgr =>
      mgr.openClientEditor()
      app.frame.addLinkComponent(mgr.clientEditor)
    }
  }
}

import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class GUIWorkspaceAction(name: String, workspace: GUIWorkspace) extends AbstractAction(name) {
  def performAction(workspace: GUIWorkspace): Unit = {}

  override def actionPerformed(e: ActionEvent): Unit = {
    performAction(workspace)
  }
}

class Open3DViewAction(workspace: GUIWorkspace) extends GUIWorkspaceAction(I18N.gui.get("menu.tools.3DView.switch"), workspace) {
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

class SimpleGUIWorkspaceAction(name: String, workspace: GUIWorkspace, action: GUIWorkspace => Unit) extends GUIWorkspaceAction(name, workspace) {
  override def performAction(workspace: GUIWorkspace): Unit = {
    action(workspace)
  }
}
