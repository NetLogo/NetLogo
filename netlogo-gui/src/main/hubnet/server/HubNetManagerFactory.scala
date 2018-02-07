// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.api.HubNetInterface
import org.nlogo.fileformat
import org.nlogo.workspace.{ AbstractWorkspace, HubNetManagerFactory }

class HeadlessHubNetManagerFactory extends HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspace): HubNetInterface = {
    val converter = fileformat.converter(
      workspace.getExtensionManager,
      workspace.getCompilationEnvironment,
      workspace.compilerServices,
      fileformat.defaultAutoConvertables) _
    new HeadlessHubNetManager(workspace,
      fileformat.standardLoader(workspace.compilerServices),
      converter(workspace.world.program.dialect))
  }
}
