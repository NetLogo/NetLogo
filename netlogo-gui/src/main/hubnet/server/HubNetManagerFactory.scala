// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.api.{ HubNetInterface, ModelLoader, NetLogoLegacyDialect }
import org.nlogo.fileformat
import org.nlogo.workspace.{ AbstractWorkspaceScala, HubNetManagerFactory }

class HeadlessHubNetManagerFactory extends HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspaceScala): HubNetInterface =
    new HeadlessHubNetManager(workspace,
      fileformat.standardLoader(workspace,
        fileformat.ModelConverter(workspace.getExtensionManager, workspace.getCompilationEnvironment, workspace, NetLogoLegacyDialect)))
}
