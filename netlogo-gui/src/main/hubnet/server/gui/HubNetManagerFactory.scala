// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import org.nlogo.api.{ HubNetInterface, ModelLoader, NetLogoLegacyDialect }
import org.nlogo.fileformat
import org.nlogo.workspace.{ AbstractWorkspaceScala, HubNetManagerFactory => WSHubNetManagerFactory }
import org.nlogo.window.{ EditorFactory, GUIWorkspace, InterfaceFactory, MenuBarFactory }

import java.awt.Component

class HubNetManagerFactory(linkParent: Component,
                       editorFactory: EditorFactory,
                       ifactory: InterfaceFactory,
                       menuFactory: MenuBarFactory) extends WSHubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspaceScala): HubNetInterface = {
    workspace match {
      case g: GUIWorkspace =>
        val loader =
          fileformat.standardLoader(workspace,
            fileformat.ModelConverter(workspace.getExtensionManager, workspace.getCompilationEnvironment, workspace, NetLogoLegacyDialect))
        new GUIHubNetManager(g, linkParent, editorFactory, ifactory, menuFactory, loader)
      case _ => throw new Exception("Expected GUIWorkspace, got: " + workspace)
    }
  }
}
