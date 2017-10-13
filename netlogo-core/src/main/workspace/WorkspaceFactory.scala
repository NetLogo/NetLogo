// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.nvm.Workspace
import org.nlogo.api.Version

trait CurrentModelOpener {
  def openCurrentModelIn(workspace: Workspace): Unit
  def currentVersion: Version
}

trait WorkspaceFactory {
  def newInstance(is3D: Boolean): AbstractWorkspace
}
