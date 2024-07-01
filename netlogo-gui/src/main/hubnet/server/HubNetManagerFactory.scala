// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.api.HubNetInterface
import org.nlogo.fileformat
import org.nlogo.workspace.{ AbstractWorkspaceScala, HubNetManagerFactory }

class HeadlessHubNetManagerFactory extends HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspaceScala): HubNetInterface = {
    val converter = fileformat.converter( workspace.getExtensionManager
                                        , workspace.getLibraryManager
                                        , workspace.getCompilationEnvironment
                                        , workspace
                                        , fileformat.defaultAutoConvertables) _
    new HeadlessHubNetManager(workspace, fileformat.standardAnyLoader(workspace), converter(workspace.world.program.dialect))
  }
}
