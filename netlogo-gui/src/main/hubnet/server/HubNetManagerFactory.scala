// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.api.HubNetInterface
import org.nlogo.fileformat.FileFormat
import org.nlogo.workspace.{ AbstractWorkspaceScala, HubNetManagerFactory }

class HeadlessHubNetManagerFactory extends HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspaceScala): HubNetInterface = {
    val converter = FileFormat.converter( workspace.getExtensionManager
                                        , workspace.getLibraryManager
                                        , workspace.getCompilationEnvironment
                                        , workspace
                                        , FileFormat.defaultAutoConvertables) _
    new HeadlessHubNetManager(workspace, FileFormat.standardAnyLoader(workspace), converter(workspace.world.program.dialect))
  }
}
