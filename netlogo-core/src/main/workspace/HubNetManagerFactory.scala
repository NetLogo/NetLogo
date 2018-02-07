// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.HubNetInterface

trait HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspace): HubNetInterface
}
