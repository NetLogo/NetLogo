// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that
 each have their own menu bar and menus ev 8/25/05 */

import org.nlogo.core.AgentKind
import org.nlogo.core.I18N

class ToolsMenu(app: App) extends org.nlogo.swing.Menu(I18N.gui.get("menu.tools")) {

  implicit val i18nName = I18N.Prefix("menu.tools")

  setMnemonic('T')
  addMenuItem(I18N.gui("halt"), app.workspace.halt _)
  addSeparator()
  addMenuItem(I18N.gui("globalsMonitor"), () => app.workspace.inspectAgent(AgentKind.Observer))
  addMenuItem(I18N.gui("turtleMonitor"), () => app.workspace.inspectAgent(AgentKind.Turtle))
  addMenuItem(I18N.gui("patchMonitor"), () => app.workspace.inspectAgent(AgentKind.Patch))
  addMenuItem(I18N.gui("linkMonitor"), () => app.workspace.inspectAgent(AgentKind.Link))
  addMenuItem(I18N.gui("closeAllAgentMonitors"), app.workspace.closeAgentMonitors _)
  addMenuItem(I18N.gui("closeDeadAgentMonitors"), app.workspace.stopInspectingDeadAgents _)
  addSeparator()
  addMenuItem('/', app.tabs.interfaceTab.commandCenterAction)
  addSeparator()
  addMenuItem(I18N.gui("3DView"), 'T', true, open3DView _)
  addMenuItem(I18N.gui("colorSwatches"), openColorDialog _)
  addMenuItem(I18N.gui("turtleShapesEditor"),
              () => app.turtleShapesManager.init(I18N.gui("turtleShapesEditor")))
  addMenuItem(I18N.gui("linkShapesEditor"),
              () => app.linkShapesManager.init(I18N.gui("linkShapesEditor")))
  addMenuItem(app.previewCommandsEditor.title, 'P', true, () =>
    app.workspace.previewCommands =
      app.previewCommandsEditor.getPreviewCommands(new ModelSaver(app).save, app.workspace.getModelPath))
  addMenuItem(I18N.gui("behaviorSpace"), 'B', true, () => app.labManager.show())
  addMenuItem(I18N.gui("systemDynamicsModeler"), 'D', true, app.aggregateManager.showEditor _)
  addSeparator()
  addMenuItem(I18N.gui("hubNetClientEditor"), openHubNetClientEditor _)
  addMenuItem('H', true, app.workspace.hubNetControlCenterAction)

  def openColorDialog() {
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
  def open3DView() {
    try {
      app.workspace.glView.open()
      app.workspace.set2DViewEnabled(false)
    }
    catch {
      case ex: org.nlogo.window.JOGLLoadingException =>
        org.nlogo.swing.Utils.alert("3D", ex.getMessage, "" + ex.getCause, I18N.gui("common.buttons.continue") )
    }
  }
  def openHubNetClientEditor() {
    app.workspace.getHubNetManager.openClientEditor()
    app.frame.addLinkComponent(app.workspace.getHubNetManager.clientEditor)
  }
}
