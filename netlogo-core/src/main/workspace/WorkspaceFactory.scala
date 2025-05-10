// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.nvm.Workspace

trait WorkspaceFactory {
  def newInstance: AbstractWorkspace
  def openCurrentModelIn(workspace: Workspace): Unit
}
