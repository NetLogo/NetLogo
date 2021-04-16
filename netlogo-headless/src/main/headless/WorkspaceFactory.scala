package org.nlogo.headless

import org.nlogo.workspace.AbstractWorkspace

class WorkspaceFactory extends org.nlogo.workspace.WorkspaceFactory {
  def newInstance: AbstractWorkspace = HeadlessWorkspace.newInstance
}
