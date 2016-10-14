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
        val converter =
          fileformat.converter(workspace.getExtensionManager, workspace.getCompilationEnvironment,
            workspace, fileformat.defaultAutoConvertables) _
        val loader = fileformat.standardLoader(workspace)
        new GUIHubNetManager(g, linkParent, editorFactory, ifactory, menuFactory, loader, converter(NetLogoLegacyDialect))
      case _ => throw new Exception("Expected GUIWorkspace, got: " + workspace)
    }
  }
}
