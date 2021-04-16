// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.nvm.Workspace

trait CurrentModelOpener {
  def openCurrentModelIn(workspace: Workspace): Unit
}

trait WorkspaceFactory {
  def newInstance: AbstractWorkspace
}
