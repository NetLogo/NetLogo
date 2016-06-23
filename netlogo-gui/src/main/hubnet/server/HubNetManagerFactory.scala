// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.api.{ HubNetInterface, ModelLoader }
import org.nlogo.fileformat
import org.nlogo.workspace.{ AbstractWorkspaceScala, HubNetManagerFactory }

class HeadlessHubNetManagerFactory extends HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspaceScala): HubNetInterface =
    new HeadlessHubNetManager(workspace,
      fileformat.standardLoader(workspace, workspace.getExtensionManager, workspace.getCompilationEnvironment))
}
